package com.onsoim.keyword.digger.trend.infrastructure.google

import com.onsoim.keyword.digger.trend.domain.RelatedNews
import com.onsoim.keyword.digger.trend.domain.TrendingKeyword

import org.springframework.stereotype.Component
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory


@Component
class GoogleTrendsRssParser {

    fun parse(xml: String): List<TrendingKeyword> {
        require(xml.isNotBlank()) {
            "Google Trends RSS XML is empty."
        }

        val document = try {
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                isXIncludeAware = false
                isExpandEntityReferences = false

                setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
                setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true
                )
                setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false
                )
                setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    false
                )

                setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
                setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
            }

            factory.newDocumentBuilder().parse(
                InputSource(StringReader(xml))
            )
        } catch (exception: Exception) {
            throw IllegalArgumentException(
                "Failed to parse Google Trends RSS XML.",
                exception
            )
        }

        val root = document.documentElement
            ?: throw IllegalArgumentException("RSS root element is missing.")

        require(root.elementName() == "rss") {
            "Invalid RSS document."
        }

        val channel =
            root.directChild("channel") ?: throw IllegalArgumentException("RSS channel is missing.")

        return channel.directChildren("item").mapNotNull itemLoop@{ item ->

            val keyword = item.directChildText("title") ?: return@itemLoop null

            val relatedNews =
                item.directChildren("news_item", HT_NAMESPACE).mapNotNull newsLoop@{ news ->

                    val title = news.directChildText("news_item_title", HT_NAMESPACE)
                        ?: return@newsLoop null

                    RelatedNews(
                        title = title,
                        url = news.directChildText("news_item_url", HT_NAMESPACE),
                        source = news.directChildText("news_item_source", HT_NAMESPACE),
                        snippet = news.directChildText("news_item_snippet", HT_NAMESPACE),
                    )
                }

            TrendingKeyword(
                keyword = keyword,
                trendUrl = "https://trends.google.com/trends/explore?q=${
                    URLEncoder.encode(
                        keyword,
                        StandardCharsets.UTF_8
                    )
                }",
                approximateTraffic = item.directChildText("approx_traffic", HT_NAMESPACE),
                startedAt = item.directChildText("pubDate")?.let(::parseDate),
                relatedNews = relatedNews
            )
        }
    }

    private fun parseDate(value: String): OffsetDateTime? {
        return runCatching {
            ZonedDateTime.parse(
                value,
                DateTimeFormatter.RFC_1123_DATE_TIME
            ).toOffsetDateTime()
        }.getOrNull()
    }

    private fun Element.directChild(
        localName: String,
        namespaceUri: String? = null
    ): Element? {
        return directChildren(localName, namespaceUri).firstOrNull()
    }

    private fun Element.directChildText(
        localName: String,
        namespaceUri: String? = null,
    ): String? {
        return directChild(localName, namespaceUri)
            ?.textContent
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun Element.directChildren(
        localName: String,
        namespaceUri: String? = null,
    ): List<Element> {
        val result = mutableListOf<Element>()

        for (index in 0 until childNodes.length) {
            val child = childNodes.item(index) as? Element ?: continue

            val childName = child.elementName()

            val namespaceMatches = if (namespaceUri == null) {
                child.namespaceURI.isNullOrEmpty()
            } else {
                child.namespaceURI == namespaceUri
            }

            if (childName == localName && namespaceMatches) {
                result.add(child)
            }
        }

        return result
    }

    private fun Element.elementName(): String {
        return localName ?: nodeName.substringAfter(':')
    }

    companion object {
        private const val HT_NAMESPACE = "https://trends.google.com/trending/rss"
    }
}