"use strict"

const i18n = require("i18n");
const alexa = require("alexa-app");
const _ = require("underscore");
const express = require('express');
const bodyParser = require('body-parser');
const PubSub = require('pubsub-js');
PubSub.immediateExceptions = true;

const OracleBot = require('@oracle/bots-node-sdk');
const MessageModel = OracleBot.Lib.MessageModel;
const messageModelUtil = OracleBot.Util.MessageModel;
const botUtil = OracleBot.Util.Text;
const webhookUtil = OracleBot.Util.Webhook;

const supportScreen = 'Alexa.Presentation.APL';

module.exports = new function() {
    i18n.configure({
        locales: ['en-US', 'fr-FR'],
        directory: __dirname + '/locales',
        autoReload: true,
        register: global,
        defaultLocale: 'en-US'
    });
    var self = this;

    function isSupportedScreen(alexa_req) {
        if (alexa_req.context &&
            (alexa_req.context[supportScreen] ||
                (alexa_req.context.System && alexa_req.context.System.device && alexa_req.context.System.device.supportedInterfaces && alexa_req.context.System.device.supportedInterfaces[supportScreen])))
            alexa_req.getSession().set('supportScreen', true);
    }

    //replace these settings to point to your webhook channel
    var metadata = {
        allowConfigUpdate: false, //set to false to turn off REST endpoint of allowing update of metadata
        waitForMoreResponsesMs: 200, //milliseconds to wait for additional webhook responses
        amzn_appId: "amzn1.ask.skill.xxxxxxxxxxxxxxxxx",
        channelSecretKey: "xxxxxxxxxxxxxxxxxxx", // ODA channel bot secret
        channelUrl: "https://botvs-mpaasocimt.botmxp.ocp.oraclecloud.com:443/connectors/v1/tenants/idcs-61f4db15e8e96c/listeners/webhook/channels/xxxxxxxx-b54f-43f4-80ac-xxxxxxxxxxx" // ODA channel bot URL
    };

    this.randomIntInc = function(low, high) {
        return Math.floor(Math.random() * (high - low + 1) + low);
    };

    this.setConfig = function(config) {
        metadata = _.extend(metadata, _.pick(config, _.keys(metadata)));
    }

    // expose this function to be stubbed
    this.sendWebhookMessageToBot = function(channelUrl, channelSecretKey, userId, messagePayload, additionalProperties, callback) {
        webhookUtil.messageToBotWithProperties(channelUrl, channelSecretKey, userId, messagePayload, additionalProperties, callback);
    };

    this.init = function(config) {

        var app = express();
        var alexaRouter = express.Router();
        alexaRouter.use(bodyParser.json());
        // load routers with body-parser applied
        //var appRouter = OracleBot.Middleware.webhookReceiver();
        //var alexaRouter = OracleBot.Middleware.webhookReceiver();
        //app.use(appRouter);
        app.use('/alexa', alexaRouter);
        var logger = (config ? config.logger : null);
        if (!logger) {
            logger = console;
        }

        if (metadata.channelUrl && metadata.channelSecretKey) {
            logger.info('Alexa bot - Using Channel:', metadata.channelUrl);
        }

        // compile the list of actions, global actions and other menu options
        function menuResponseMap(resp, card) {
            var responseMap = {};

            function addToMap(label, type, action) {
                responseMap[label] = {
                    type: type,
                    action: action
                };
            }

            if (!card) {
                if (resp.globalActions && resp.globalActions.length > 0) {
                    resp.globalActions.forEach(function(gAction) {
                        addToMap(gAction.label, 'global', gAction);
                    });
                }
                if (resp.actions && resp.actions.length > 0) {
                    resp.actions.forEach(function(action) {
                        addToMap(action.label, 'message', action);
                    });
                }
                if (resp.type === 'card' && resp.cards && resp.cards.length > 0) {
                    resp.cards.forEach(function(card) {
                        //special menu option to navigate to card detail
                        addToMap('Card ' + card.title, 'card', {
                            type: 'custom',
                            value: {
                                type: 'card',
                                value: card
                            }
                        });
                    });
                }
            } else {
                if (card.actions && card.actions.length > 0) {
                    card.actions.forEach(function(action) {
                        addToMap(action.label, 'message', action);
                    });
                }
                //special menu option to return to main message from the card
                addToMap('Return', 'cardReturn', {
                    type: 'custom',
                    value: {
                        type: 'messagePayload',
                        value: resp
                    }
                });
            }
            return responseMap;
        }

        if (metadata.allowConfigUpdate) {
            app.put('/config', bodyParser.json(), function(req, res) {
                let config = req.body;
                logger.info(config);
                if (config) {
                    self.setConfig(config);
                }
                res.sendStatus(200).send();
            });
        }

        function getChannelSecretKey(req) {
            console.log("Alexa getChannelSecretKey", metadata.channelSecretKey);
            return metadata.channelSecretKey;
        }

        OracleBot.init(app);
        app.post('/botWebhook/messages',
            OracleBot.Middleware.webhookReceiver(
                getChannelSecretKey,
                (req, res) => {
                    res.sendStatus(200);
                    const userID = req.body.userId;
                    logger.info("Publishing to", userID);
                    PubSub.publish(userID, req.body);
                }
            )
        );

        var alexa_app = new alexa.app("app");

        alexa_app.intent("CommandBot", {},
            function(alexa_req, alexa_res) {
                logger.info("alexa_app.intent");
                var command = alexa_req.slot("command");
                return (execute_call_bot(alexa_req, alexa_res, command, false));
            }
        );

        var execute_call_bot = function(alexa_req, alexa_res, command, shouldEndSession) {
            var session = alexa_req.getSession();
            isSupportedScreen(alexa_req);
            var locale = alexa_req.data.request.locale;
            i18n.setLocale(locale);
            //var userId = session.get("userId");
            var userId = session.get('sessionId');
            if (!userId) {
                //userId = session.details.userId;
                userId = session.details.user.userId;
                if (!userId) {
                    userId = self.randomIntInc(1000000, 9999999).toString();
                }
                session.set("userId", userId);
            }
            console.log('-------------');
            console.log(userId);
            console.log('-------------');
            alexa_res.shouldEndSession(shouldEndSession);
            if (metadata.channelUrl && metadata.channelSecretKey && userId && command) {
                const userIdTopic = userId;
                var respondedToAlexa = false;
                var additionalProperties = {
                    "profile": {
                        "clientType": "alexa",
                        "locale": locale
                    }
                };
                var sendToAlexa = function(resolve, reject) {
                    if (!respondedToAlexa) {
                        respondedToAlexa = true;
                        logger.info('Prepare to send to Alexa');
                        //alexa_res.send();
                        resolve();
                        PubSub.unsubscribe(userIdTopic);
                    } else {
                        logger.info("Already sent response");
                    }
                };
                // compose text response to alexa, and also save botMessages and botMenuResponseMap to alexa session so they can be used to control menu responses next
                var navigableResponseToAlexa = function(resp) {
                    var respModel;
                    if (resp.messagePayload) {
                        respModel = new MessageModel(resp.messagePayload);
                    } else {
                        // handle 1.0 webhook format as well
                        respModel = new MessageModel(resp);
                    }
                    var botMessages = session.get("botMessages");
                    if (!Array.isArray(botMessages)) {
                        botMessages = [];
                    }
                    var botMenuResponseMap = session.get("botMenuResponseMap");
                    if (typeof botMenuResponseMap !== 'object') {
                        botMenuResponseMap = {};
                    }
                    botMessages.push(respModel.messagePayload());
                    session.set("botMessages", botMessages);
                    session.set("botMenuResponseMap", Object.assign(botMenuResponseMap || {}, menuResponseMap(respModel.messagePayload())));
                    let messageToAlexa = messageModelUtil.convertRespToText(respModel.messagePayload(), locale);
                    //logger.debug("Message to Alexa (navigable):"+ messageToAlexa);
                    alexa_res.say(messageToAlexa);
                    let aplToAlexa = messageModelUtil.convertRespToDirective(respModel.messagePayload());
                    if (aplToAlexa.document && aplToAlexa.datasources && session.get('supportScreen')) {
                        logger.info("Message to Alexa (APL):" + aplToAlexa);
                        alexa_res.directive({
                            type: 'Alexa.Presentation.APL.RenderDocument',
                            //token: '[SkillProvidedToken]',
                            version: '1.0',
                            document: aplToAlexa.document,
                            datasources: aplToAlexa.datasources
                        });
                    }
                };

                var sendMessageToBot = function(messagePayload) {
                    logger.info('Creating new promise for', messagePayload);
                    return new Promise(function(resolve, reject) {
                        var commandResponse = function(msg, data) {
                            logger.info('Received callback message from webhook channel');
                            var resp = data;
                            logger.info('Parsed Message Body:', resp);
                            if (!respondedToAlexa) {
                                navigableResponseToAlexa(resp);
                            } else {
                                logger.info("Already processed response");
                                return;
                            }
                            if (metadata.waitForMoreResponsesMs) {
                                _.delay(function() {
                                    sendToAlexa(resolve, reject);
                                }, metadata.waitForMoreResponsesMs);
                            } else {
                                sendToAlexa(resolve, reject);
                            }
                        };
                        var token = PubSub.subscribe(userIdTopic, commandResponse);
                        self.sendWebhookMessageToBot(metadata.channelUrl, metadata.channelSecretKey, userId, messagePayload, additionalProperties, function(err) {
                            if (err) {
                                logger.info("Failed sending message to Bot");
                                alexa_res.say("Failed sending message to Bot.  Please review your bot configuration.");
                                reject();
                                PubSub.unsubscribe(userIdTopic);
                            }
                        });
                    });
                };
                var handleInput = function(input) {
                    var botMenuResponseMap = session.get("botMenuResponseMap");
                    if (typeof botMenuResponseMap !== 'object') {
                        botMenuResponseMap = {};
                    }
                    var menuResponse = botUtil.approxTextMatch(input, _.keys(botMenuResponseMap), true, true, .7);
                    var botMessages = session.get("botMessages");
                    //if command is a menu action
                    if (menuResponse) {
                        var menu = botMenuResponseMap[menuResponse.item];
                        // if it is global action or message level action
                        if (['global', 'message'].includes(menu.type)) {
                            var action = menu.action;
                            session.set("botMessages", []);
                            session.set("botMenuResponseMap", {});
                            if (action.type === 'postback') {
                                var postbackMsg = MessageModel.postbackConversationMessage(action.postback);
                                return sendMessageToBot(postbackMsg);
                            } else if (action.type === 'location') {
                                logger.info('Sending a predefined location to bot');
                                return sendMessageToBot(MessageModel.locationConversationMessage(37.2900055, -121.906558));
                            }
                            // if it is navigating to card detail
                        } else if (menu.type === 'card') {
                            var selectedCard;
                            if (menu.action && menu.action.type && menu.action.type === 'custom' && menu.action.value && menu.action.value.type === 'card') {
                                selectedCard = _.clone(menu.action.value.value);
                            }
                            if (selectedCard) {
                                if (!Array.isArray(botMessages)) {
                                    botMessages = [];
                                }
                                var selectedMessage;
                                if (botMessages.length === 1) {
                                    selectedMessage = botMessages[0];
                                } else {
                                    selectedMessage = _.find(botMessages, function(botMessage) {
                                        if (botMessage.type === 'card') {
                                            return _.some(botMessage.cards, function(card) {
                                                return (card.title === selectedCard.title);
                                            });
                                        } else {
                                            return false;
                                        }
                                    });
                                }
                                if (selectedMessage) {
                                    //session.set("botMessages", [selectedMessage]);
                                    session.set("botMenuResponseMap", menuResponseMap(selectedMessage, selectedCard));
                                    //let messageToAlexa = messageModelUtil.cardToText(selectedCard, 'Card', locale);
				    let messageToAlexa = messageModelUtil.cardToText(selectedCard, '', locale);
                                    logger.info("Message to Alexa (card):", messageToAlexa);
                                    alexa_res.say(messageToAlexa);
                                    return alexa_res.send();
                                }
                            }
                            // if it is navigating back from card detail
                        } else if (menu.type === 'cardReturn') {
                            var returnMessage;
                            if (menu.action && menu.action.type && menu.action.type === 'custom' && menu.action.value && menu.action.value.type === 'messagePayload') {
                                returnMessage = _.clone(menu.action.value.value);
                            }
                            if (returnMessage) {
                                //session.set("botMessages", [returnMessage]);
                                session.set("botMenuResponseMap", _.reduce(botMessages, function(memo, msg) {
                                    return Object.assign(memo, menuResponseMap(msg));
                                }, {}));
                                //session.set("botMenuResponseMap", menuResponseMap(returnMessage));
                                _.each(botMessages, function(msg) {
                                    let messageToAlexa = messageModelUtil.convertRespToText(msg, locale);
                                    logger.info("Message to Alexa (return from card):", messageToAlexa);
                                    alexa_res.say(messageToAlexa);
                                })
                                return alexa_res.send();
                            }
                        }
                    } else {
                        var commandMsg = MessageModel.textConversationMessage(command);
                        return sendMessageToBot(commandMsg);
                    }
                };
                return handleInput(command);
            } else {
                _.defer(function() {
                    alexa_res.say("I don't understand. Could you please repeat what you want?");
                    //alexa_res.send();
                });
            }
            //return false;
        }

        alexa_app.intent("AMAZON.StopIntent", {},
            function(alexa_req, alexa_res) {
                var locale = alexa_req.data.request.locale;
                var command = i18n.__('bye');
                return (execute_call_bot(alexa_req, alexa_res, command, true));
            }
        );

        alexa_app.intent("AMAZON.CancelIntent", {},
            function(alexa_req, alexa_res) {
                var locale = alexa_req.data.request.locale;
                var command = i18n.__('bye');
                return (execute_call_bot(alexa_req, alexa_res, command, true));
            }
        );

        alexa_app.launch(function(alexa_req, alexa_res) {
            logger.info("alexa_app.launch");
            //alexa_res.say(i18n.__('WelcomeToBot'));
            //var card = {'type':'Simple', 'title':i18n.__('WelcomeToBot'), 'content':i18n.__('WelcomeToBotCard')};
            //alexa_res.card(card);
            var command = i18n.__('hello');
            return (execute_call_bot(alexa_req, alexa_res, command, false));
        });

        alexa_app.pre = function(alexa_req, alexa_res, alexa_type) {
            logger.debug(alexa_req.data.session.application.applicationId);
            logger.debug(metadata.amzn_appId);
            // change the application id
            if (alexa_req.data.session.application.applicationId != metadata.amzn_appId) {
                logger.error("fail as application id is not valid");
                alexa_res.fail(i18n.__("InvalidapplicationId"));
            }
            logger.info(JSON.stringify(alexa_req.data, null, 4));
            if (!metadata.channelUrl || !metadata.channelSecretKey) {
                var message = "The bot cannot respond.  Please check the channel and secret key configuration.";
                alexa_res.fail(message);
                logger.info(message);
            }
        };
        //alexa_app.express(alexaRouter, "/", true);
        alexa_app.express({
            router: alexaRouter,
            checkCert: false
        });

        app.locals.endpoints = [];
        app.locals.endpoints.push({
            name: 'webhook',
            method: 'POST',
            endpoint: '/botWebhook/messages'
        });
        app.locals.endpoints.push({
            name: 'alexa',
            method: 'POST',
            endpoint: '/alexa/app'
        });

        return app;
    };

    return this;

}();
