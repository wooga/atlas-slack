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

package wooga.gradle.slack

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.slf4j.Logger
import wooga.gradle.slack.internal.DefaultSlackPluginExtension
import wooga.gradle.slack.tasks.Slack

class SlackPlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(SlackPlugin)

    static String EXTENSION_NAME = "slack"

    @Override
    void apply(Project project) {
        def extension = create_and_configure_extension(project)

        def sendMessageTask = project.tasks.create("sendMessage", Slack)
        sendMessageTask.group = "slack"
        sendMessageTask.description = "sends a slack message"


        project.tasks.withType(Slack, new Action<Slack>() {
            @Override
            void execute(Slack slack) {
                slack.webhook.set(extension.webhook)
                slack.username.set(extension.username)
                slack.icon.set(extension.icon)
            }
        })
    }

    protected static SlackPluginExtension create_and_configure_extension(Project project) {
        def extension = project.extensions.create(SlackPluginExtension, EXTENSION_NAME, DefaultSlackPluginExtension, project)

        extension.username.set(project.provider({
            String username = (project.properties[SlackConsts.USERNAME_OPTION]
                    ?: System.getenv()[SlackConsts.USERNAME_ENV_VAR]) as String
            if (!username) {
                username = "Gradle"
            }
            username
        }))

        extension.icon.set(project.provider({
            String url = (project.properties[SlackConsts.ICON_OPTION]
                    ?: System.getenv()[SlackConsts.ICON_ENV_VAR]) as String
            if (!url) {
                url = SlackConsts.DEFAULT_ICON
            }

            new URL(url)
        }))

        extension.webhook.set(project.provider({
            String url = (project.properties[SlackConsts.WEBHOOK_OPTION]
                    ?: System.getenv()[SlackConsts.WEBHOOK_ENV_VAR]) as String
            if (url) {
                return new URL(url)
            }
            null
        }))

        extension
    }
}
