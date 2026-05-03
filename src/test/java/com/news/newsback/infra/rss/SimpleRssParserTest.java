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
    @DisplayName("RSS item 제목 또는 URL이 비어 있으면 해당 item만 스킵한다")
    void RSS_파싱_제목_URL_빈값_스킵() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                    <channel>
                        <item>
                            <title></title>
                            <description>Content 1</description>
                            <link>https://example.com/1</link>
                        </item>
                        <item>
                            <title>News 2</title>
                            <description>Content 2</description>
                            <link>https://example.com/2</link>
                        </item>
                    </channel>
                </rss>
                """;

        List<News> results = rssParser.parse(xml, "TestPublisher", "ko", "KR");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("News 2");
    }

    @Test
    @DisplayName("RSS 텍스트에 남은 HTML entity 토큰을 정규화한다")
    void RSS_파싱_HTML_ENTITY_정규화() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                    <channel>
                        <item>
                            <title>62년 만의 노동절hellip;우상호의 약속 ldquo;저녁 있는 삶, 안전한 일터rdquo;</title>
                            <description>국민의힘 경북도당 광역middot;기초의원 공천결과 발표</description>
                            <link>https://www.pressian.com/pages/articles/2026050113122241108&amp;amp;ref=rss</link>
                        </item>
                    </channel>
                </rss>
                """;

        List<News> results = rssParser.parse(xml, "TestPublisher", "ko", "KR");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("62년 만의 노동절…우상호의 약속 “저녁 있는 삶, 안전한 일터”");
        assertThat(results.get(0).getContent()).isEqualTo("국민의힘 경북도당 광역·기초의원 공천결과 발표");
        assertThat(results.get(0).getUrl()).isEqualTo("https://www.pressian.com/pages/articles/2026050113122241108&ref=rss");
    }

    @Test
    @DisplayName("description이 없으면 제목을 본문 fallback으로 사용한다")
    void RSS_파싱_DESCRIPTION_없으면_제목_FALLBACK() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                    <channel>
                        <item>
                            <title>한경 뉴스 제목</title>
                            <link>https://www.hankyung.com/article/1</link>
                        </item>
                    </channel>
                </rss>
                """;

        List<News> results = rssParser.parse(xml, "TestPublisher", "ko", "KR");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getContent()).isEqualTo("한경 뉴스 제목");
    }

    @Test
    @DisplayName("description이 HTML 이미지뿐이면 dc:subject를 본문 fallback으로 사용한다")
    void RSS_파싱_HTML_DESCRIPTION이면_DC_SUBJECT_FALLBACK() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/">
                    <channel>
                        <item>
                            <title>한겨레 뉴스 제목</title>
                            <description><![CDATA[<table><tr><td><img src=https://example.com/a.webp border=0></td></tr></table>]]></description>
                            <link>https://www.hani.co.kr/arti/1.html</link>
                            <dc:subject>한겨레 뉴스 본문 대체 텍스트</dc:subject>
                        </item>
                    </channel>
                </rss>
                """;

        List<News> results = rssParser.parse(xml, "TestPublisher", "ko", "KR");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getContent()).isEqualTo("한겨레 뉴스 본문 대체 텍스트");
    }

    @Test
    @DisplayName("DOCTYPE이 포함된 정상 RSS XML도 파싱한다")
    void RSS_파싱_성공_DOCTYPE_포함() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE rss [
                    <!ELEMENT rss ANY>
                ]>
                <rss version="2.0">
                    <channel>
                        <item>
                            <title>News 1</title>
                            <description>Content 1</description>
                            <link>https://example.com/1</link>
                        </item>
                    </channel>
                </rss>
                """;

        List<News> results = rssParser.parse(xml, "TestPublisher", "ko", "KR");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("News 1");
        assertThat(results.get(0).getContent()).isEqualTo("Content 1");
    }
}
