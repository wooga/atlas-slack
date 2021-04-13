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

class SlackConsts {

    static String WEBHOOK_OPTION = "slack.webhook"
    static String WEBHOOK_ENV_VAR = "SLACK_WEBHOOK"

    static String USERNAME_OPTION = "slack.username"
    static String USERNAME_ENV_VAR = "SLACK_USERNAME"

    static String ICON_OPTION = "slack.icon"
    static String ICON_ENV_VAR = "SLACK_ICON"

    static String DEFAULT_ICON = "https://raw.githubusercontent.com/alexleventer/gradle-slack-plugin/master/assets/gradlephant.png"
}
