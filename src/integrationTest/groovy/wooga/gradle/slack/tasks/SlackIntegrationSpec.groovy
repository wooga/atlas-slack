package wooga.gradle.slack.tasks

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.junit.Rule
import spock.lang.Unroll
import wooga.gradle.slack.IntegrationSpec
import wooga.gradle.slack.SlackConsts
import wooga.gradle.slack.SlackPlugin

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

class SlackIntegrationSpec extends IntegrationSpec {

    @Rule
    public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort(), false)

    def setup() {
        //fetch webhook url from environment
        environmentVariables.set(SlackConsts.WEBHOOK_ENV_VAR, "http://localhost:${wireMock.port()}/services")

        buildFile << """
            ${applyPlugin(SlackPlugin)}
        """.stripIndent()
    }

    def "task :sendMessage skips when webhook, message or payload is not set"() {
        when:
        def result = runTasksSuccessfully("sendMessage")

        then:
        result.wasSkipped("sendMessage")
    }

    @Unroll
    def "sends slack message with #cliSwitch and sets correct payload key #payloadKey"() {
        given: "A stubbed ok response from the mocked server"
        wireMock.stubFor(post(urlEqualTo("/services"))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody("ok")))

        when:
        runTasksSuccessfully("sendMessage", cliSwitch, value, "--message", "test message")

        then:
        wireMock.verify(postRequestedFor(urlEqualTo("/services"))
                .withRequestBody(containing(""" "${payloadKey}":"${value}" """.trim()))
                .withHeader("Content-Type", equalToIgnoreCase("application/json; charset=UTF-8")))

        where:
        cliSwitch    | value                          | payloadKey
        "--username" | "TestUser"                     | "username"
        "--icon"     | "https://icons.com/custom.png" | "icon_url"
        "--color"    | "#FF00FF"                      | "color"

    }

    @Unroll
    def "send slack message #message"() {
        given: "A stubbed ok response from the mocked server"
        wireMock.stubFor(post(urlEqualTo("/services"))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody("ok")))

        and: "some properties in the build gradle"
        buildFile << """
        version = "1.0.0"
        ext.foo = "bar"
        ext.bar = ["baz":"faz"]
        """.stripIndent()

        when:
        runTasksSuccessfully("sendMessage", "--message", message)

        then:
        wireMock.verify(postRequestedFor(urlEqualTo("/services"))
                .withRequestBody(containing(expectedMessage))
                .withHeader("Content-Type", equalToIgnoreCase("application/json; charset=UTF-8")))

        where:
        message                                                  | expectedMessage
        "can replace gradle properties like version: #{version}" | "can replace gradle properties like version: 1.0.0"
        "custom properties: `#{foo}`"                            | "custom properties: `bar`"
        "deep nested object: `#{bar.baz}`"                       | "deep nested object: `faz`"
        "deep nested with unknown key: `#{bar.x}`"               | "deep nested with unknown key: `null`"

    }

    def "can set #property in extension"() {
        given: "A stubbed ok response from the mocked server"
        wireMock.stubFor(post(urlEqualTo("/services"))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody("ok")))

        and: "a confgured value in the extension"
        buildFile << """
        slack.${property} = "${value}"
        """.stripIndent()

        when:
        runTasksSuccessfully("sendMessage", "--message", "test message")

        then:
        wireMock.verify(postRequestedFor(urlEqualTo("/services"))
                .withRequestBody(containing(""" "${payloadKey}":"${value}" """.trim()))
                .withHeader("Content-Type", equalToIgnoreCase("application/json; charset=UTF-8")))

        where:
        property | value                                                                                          | payloadKey
        "icon"   | "http://icons.iconarchive.com/icons/blackvariant/button-ui-system-apps/1024/Terminal-icon.png" | "icon_url"
    }

    @Unroll
    def "can set webhook in #location"() {
        given: "A stubbed ok response from the mocked server"
        wireMock.stubFor(post(urlEqualTo("/custom/services"))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody("ok")))

        and: "a configured webhook in the extension"
        buildFile << """
        ${name}.webhook = "http://localhost:${wireMock.port()}/custom/services"
        """.stripIndent()

        when:
        runTasksSuccessfully("sendMessage", "--message", "test message")

        then:
        wireMock.verify(postRequestedFor(urlEqualTo("/custom/services"))
                .withRequestBody(containing(""" "text":"test message" """.trim()))
                .withHeader("Content-Type", equalToIgnoreCase("application/json; charset=UTF-8")))

        where:
        location    | name
        "extension" | "slack"
        "task"      | "sendMessage"

    }

    def "send slack message with message payload"() {
        given: "a custom slack message payload"
        def payload = createFile("message_payload.json")
        payload << """
        {
            "attachments": [
                {
                    "fallback": "ReferenceError - UI is not defined: https://honeybadger.io/path/to/event/",
                    "text": "<https://honeybadger.io/path/to/event/|ReferenceError> - UI is not defined",
                    "fields": [
                        {
                            "title": "Project",
                            "value": "Awesome Project",
                            "short": true
                        },
                        {
                            "title": "Environment",
                            "value": "production",
                            "short": true
                        }
                    ],
                    "color": "#F35A00"
                }
            ]
        }
        """.stripIndent()

        and: "A stubbed ok response from the mocked server"
        wireMock.stubFor(post(urlEqualTo("/services"))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody("ok")))

        when:
        runTasksSuccessfully("sendMessage", "--message-payload", "${projectDir}/message_payload.json")

        then:
        wireMock.verify(postRequestedFor(urlEqualTo("/services"))
                .withRequestBody(equalTo(payload.text))
                .withHeader("Content-Type", equalToIgnoreCase("application/json; charset=UTF-8")))

    }

    def "prints warning when send failed"() {
        given: "A stubbed ok response from the mocked server"
        wireMock.stubFor(post(urlEqualTo("/services"))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .willReturn(aResponse()
                .withStatus(404)))

        when:
        def result = runTasksSuccessfully("sendMessage", "--message", "test")

        then:
        outputContains(result, "failed to send message")
    }

    def "help prints commandline description for sendMessage"() {
        when:
        def result = runTasksSuccessfully("help", "--task", "sendMessage")

        then:
        result.standardOutput.contains("Path")
        result.standardOutput.contains("Type")
        result.standardOutput.contains("Options")
        result.standardOutput.contains("--username")
        result.standardOutput.contains("--color")
        result.standardOutput.contains("--icon")
        result.standardOutput.contains("--message")
        result.standardOutput.contains("--message-payload")
        result.standardOutput.contains("--webhook")
        result.standardOutput.contains("Description")
        result.standardOutput.contains("Group")
    }

    @Unroll
    def "fails with `#color` #message"() {
        expect:
        runTasksWithFailure("sendMessage", "--color", color, "--message", message)

        where:
        color        | message
        "#FF"        | "hex value to short"
        "#FFFFGG"    | "invalid characters"
        "FF0000"     | "missing hash sign"
        "sdaksdjajd" | "random value"
    }

    @Unroll
    def "succeeds with `#color` #message"() {
        expect:
        runTasksSuccessfully("sendMessage", "--color", color, "--message", message)

        where:
        color     | message
        "#FF00FF" | "valid hex value"
        "good"    | "valid color name"
        "warning" | "valid color name"
        "danger"  | "valid color name"
    }
}
