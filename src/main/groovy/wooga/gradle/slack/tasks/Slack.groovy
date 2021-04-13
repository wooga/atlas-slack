/*
 * Copyright 2019-2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.slack.tasks

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.Transformer
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.options.OptionValues
import org.gradle.api.provider.Provider

import java.security.InvalidParameterException
import java.util.regex.Pattern

class Slack extends DefaultTask {

    @Optional
    @Input
    private final Property<String> message

    Property<String> getMessage() {
        message
    }

    void setMessage(Provider<String> value) {
        message.set(value)
    }

    @Option(option = "message", description = "The message to send")
    void setMessage(String value) {
        message.set(value)
    }

    @Input
    private final Property<URL> webhook

    Property<URL> getWebhook() {
        webhook
    }

    void setWebhook(Provider<URL> value) {
        webhook.set(value)
    }

    @Option(option = "webhook", description = "The webhook url to send the message to")
    void setWebhook(String value) {
        webhook.set(new URL(value))
    }

    @Optional
    @Input
    private final Property<String> username

    Property<String> getUsername() {
        username
    }

    void setUsername(Provider<String> value) {
        username.set(value)
    }

    @Option(option = "username", description = "The username of the sender")
    void setUsername(String value) {
        username.set(value)
    }

    @Optional
    @Input
    private final Property<URL> icon

    Property<URL> getIcon() {
        icon
    }

    void setIcon(Provider<URL> url) {
        icon.set(url)
    }

    @Option(option = "icon", description = "The icon url for the message")
    void setIcon(String value) {
        icon.set(new URL(value))
    }

    @OptionValues('color')
    List<String> availableColorOptions() {
        ["hex color", "good", "warning", "danger"]
    }

    @Optional
    @Input
    private final Property<String> color

    Property<String> getColor() {
        color
    }

    void setColor(Provider<String> value) {
        color.set(value)
    }

    @Option(option = "color", description = "A intend color")
    void setColor(String value) {
        if (!validateColor(value)) {
            throw new InvalidParameterException("The provided value for `color` is not a hex string or one of ['good', 'warning', 'danger']")
        }
        color.set(value)
    }

    @Optional
    @InputFile
    private final RegularFileProperty messagePayload

    RegularFileProperty getMessagePayload() {
        messagePayload
    }

    void setMessagePayload(Provider<File> value) {
        messagePayload.set(project.layout.file(value))
    }

    @Option(option = "message-payload", description = "A path to a slack message payload json")
    void setMessagePayload(String value) {
        messagePayload.set(new File(value))
    }

    Slack() {
        message = project.objects.property(String)
        webhook = project.objects.property(URL)
        username = project.objects.property(String)
        icon = project.objects.property(URL)
        color = project.objects.property(String)
        messagePayload = project.objects.fileProperty()

        onlyIf(new Spec<Slack>() {
            @Override
            boolean isSatisfiedBy(Slack t) {
                return t.webhook.present && (t.message.present || t.messagePayload.present)
            }
        })
    }

    @TaskAction
    protected void send() {
        def post = webhook.get().openConnection()
        String json
        if (!messagePayload.present) {
            def message = [:]

            if (username.present) {
                message['username'] = this.username.get()
            }

            if (icon.present) {
                message["icon_url"] = this.icon.get()
            }

            message["mrkdwn"] = true

            if (color.present) {
                def attachment = [:]
                attachment["fallback"] = buildMessage()
                attachment["text"] = buildMessage()
                attachment["color"] = this.color.get()

                message['attachments'] = [attachment]

            } else {
                message['text'] = buildMessage()
            }

            json = JsonOutput.toJson(message)
        } else {
            json = messagePayload.get().asFile.text
        }

        logger.info("Send message to slack endpoint ${webhook.get()}")
        logger.info(json)

        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        post.getOutputStream().write(json.getBytes("UTF-8"))
        def response = post.getResponseCode()

        if (response != 200) {
            logger.warn("failed to send message")
        }
    }

    private Boolean validateColor(String value) {
        def colorNames = ["good", "warning", "danger"]
        if (colorNames.contains(value)) {
            return true
        }

        return value.matches(Pattern.compile(/^#[a-f0-9]{6}$/, Pattern.CASE_INSENSITIVE))
    }

    protected String buildMessage() {
        def m = message.get()

        m.replaceAll(/\#\{(.*?)\}/, { groups ->
            def keyPath = groups[1] as String
            def keys = keyPath.split(/\./)
            def value = keys.toList().inject(project.properties) { Object container, String key ->
                if (container != null) {
                    return container[key]
                }
                null
            }
            value
        })
    }
}
