package com.news.newsback.infra.rss;

import com.news.newsback.domain.news.model.News;
import com.news.newsback.domain.news.exception.NewsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SimpleRssParser 단위 테스트")
class SimpleRssParserTest {

    private final SimpleRssParser rssParser = new SimpleRssParser();

    @Test
    @DisplayName("RSS XML 파싱 성공")
    void RSS_파싱_성공() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                    <channel>
                        <title>Sample News</title>
                        <item>
                            <title>News 1</title>
                            <description>Content 1</description>
                            <link>https://example.com/1</link>
                            <pubDate>Mon, 01 Jan 2024 12:00:00 +0900</pubDate>
                        </item>
                    </channel>
                </rss>
                """;

        List<News> results = rssParser.parse(xml, "TestPublisher", "ko", "KR");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("News 1");
        assertThat(results.get(0).getSource()).isEqualTo("TestPublisher");
        assertThat(results.get(0).getLanguage()).isEqualTo("ko");
        assertThat(results.get(0).getRegion()).isEqualTo("KR");
    }

    @Test
    @DisplayName("잘못된 XML 형식인 경우 NewsException 발생")
    void RSS_파싱_실패_잘못된_XML() {
        String invalidXml = "<rss><item>unclosed tag";

        assertThatThrownBy(() -> rssParser.parse(invalidXml, "TestPublisher", "ko", "KR"))
                .isInstanceOf(NewsException.class)
                .hasMessageContaining("RSS 피드 파싱에 실패했습니다");
    }

    @Test
    @DisplayName("DOCTYPE이 포함된 XML은 XXE 방어를 위해 파싱하지 않는다")
    void XXE_방어_DOCTYPE_차단() {
        String maliciousXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE rss [
                    <!ENTITY xxe SYSTEM "file:///etc/passwd">
                ]>
                <rss version="2.0">
                    <channel>
                        <item>
                            <title>News 1</title>
                            <description>&xxe;</description>
                            <link>https://example.com/1</link>
                        </item>
                    </channel>
                </rss>
                """;

        assertThatThrownBy(() -> rssParser.parse(maliciousXml, "TestPublisher", "ko", "KR"))
                .isInstanceOf(NewsException.class)
                .hasMessageContaining("RSS 피드 파싱에 실패했습니다");
    }
}
