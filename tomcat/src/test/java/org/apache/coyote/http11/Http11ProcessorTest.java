package org.apache.coyote.http11;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import support.StubSocket;

@DisplayName("Http11Processor 클래스의")
class Http11ProcessorTest {

    @Nested
    @DisplayName("process 메서드는")
    class Process {

        @Test
        @DisplayName("GET / 요청 시 Hello World! 메세지를 응답한다.")
        void defaultRequest() {

            // given
            final var socket = new StubSocket();
            final var processor = new Http11Processor(socket);

            // when
            processor.process(socket);

            // then
            final var expected = String.join("\r\n",
                    "HTTP/1.1 200 OK ",
                    "Content-Type: text/html;charset=utf-8 ",
                    "Content-Length: 12 ",
                    "",
                    "Hello world!");

            assertThat(socket.output()).isEqualTo(expected);
        }

        @ParameterizedTest(name = "GET {0} 요청 시 {1}를 응답한다.")
        @CsvSource({"/index.html, static/index.html, text/html, 5564",
                "/css/styles.css, static/css/styles.css, text/css, 211991",
                "/js/scripts.js, static/js/scripts.js, text/javascript, 976"})
        @DisplayName("요청한 자원에 해당하는 정적 파일을 응답한다.")
        void staticFileRequest(final String uri, final String fileName, final String contentType,
                               final String contentLength) throws IOException {
            // given
            final String httpRequest = String.join("\r\n",
                    "GET " + uri + " HTTP/1.1 ",
                    "Host: localhost:8080 ",
                    "Connection: keep-alive ",
                    "",
                    "");

            final var socket = new StubSocket(httpRequest);
            final Http11Processor processor = new Http11Processor(socket);

            // when
            processor.process(socket);

            // then
            final URL resource = getResource(fileName);

            final var expected = "HTTP/1.1 200 OK \r\n" +
                    "Content-Type: " + contentType + ";charset=utf-8 \r\n" +
                    "Content-Length: " + contentLength + " \r\n" +
                    "\r\n" +
                    new String(Files.readAllBytes(new File(resource.getFile()).toPath()));

            assertThat(socket.output()).isEqualTo(expected);
        }

        @Test
        @DisplayName("존재하지 않는 파일을 요청하면 예외를 던진다.")
        void invalidStaticFile_ExceptionThrown() {
            // given
            final String httpRequest = String.join("\r\n",
                    "GET /home.page HTTP/1.1 ",
                    "Host: localhost:8080 ",
                    "Accept: text/html,*/*;q=0.1 ",
                    "Connection: keep-alive ",
                    "",
                    "");

            final var socket = new StubSocket(httpRequest);
            final Http11Processor processor = new Http11Processor(socket);

            // when & then
            assertThatThrownBy(() -> processor.process(socket))
                    .isInstanceOf(NullPointerException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"/login.html", "/login"})
        @DisplayName("GET /login 요청 시 로그인 페이지를 응답한다.")
        void loginRequest(final String uri) throws IOException {
            // given
            final String httpRequest = createLoginRequest(uri);

            final var socket = new StubSocket(httpRequest);
            final Http11Processor processor = new Http11Processor(socket);

            // when
            processor.process(socket);

            // then
            final URL resource = getResource("static/login.html");

            final var expected = "HTTP/1.1 200 OK \r\n" +
                    "Content-Type: text/html;charset=utf-8 \r\n" +
                    "Content-Length: 3796 \r\n" +
                    "\r\n" +
                    new String(Files.readAllBytes(new File(resource.getFile()).toPath()));

            assertThat(socket.output()).isEqualTo(expected);
        }

        @Test
        @DisplayName("등록된 계정으로 로그인할 수 있다.")
        void loginRequest() throws IOException {
            // given
            final String httpRequest = createLoginRequest("/login?account=gugu&password=password");
            final var socket = new StubSocket(httpRequest);
            final Http11Processor processor = new Http11Processor(socket);

            // when
            processor.process(socket);

            // then
            final URL resource = getResource("static/login.html");

            final var expected = "HTTP/1.1 200 OK \r\n" +
                    "Content-Type: text/html;charset=utf-8 \r\n" +
                    "Content-Length: 3796 \r\n" +
                    "\r\n" +
                    new String(Files.readAllBytes(new File(resource.getFile()).toPath()));

            assertThat(socket.output()).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({"account=pepper&password=password", "account=gugu&password=pwd"})
        @DisplayName("등록된 계정 정보와 다른 값으로 로그인 요청 시 예외를 던진다.")
        void invalidAccount_ExceptionThrown(final String query) {
            // given
            final String httpRequest = createLoginRequest("/login?" + query);
            final var socket = new StubSocket(httpRequest);
            final Http11Processor processor = new Http11Processor(socket);

            // when & then
            assertThatThrownBy(() -> processor.process(socket))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("유효하지 않은 유저입니다.");
        }

        private String createLoginRequest(final String uri) {
            return String.join("\r\n",
                    "GET " + uri + " HTTP/1.1 ",
                    "Host: localhost:8080 ",
                    "Accept: text/html,*/*;q=0.1 ",
                    "Connection: keep-alive ",
                    "",
                    "");
        }

        private URL getResource(final String name) {
            final URL resource = getClass().getClassLoader().getResource(name);
            assert resource != null;
            return resource;
        }

    }
}
