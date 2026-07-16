package vn.hcmute.edu.dp.nhom10.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hcmute.edu.dp.nhom10.backend.entity.Category;
import vn.hcmute.edu.dp.nhom10.backend.entity.Collection;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.repository.CategoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CollectionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;

import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j(topic = "SEO-CONTROLLER")
public class SeoController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CollectionRepository collectionRepository;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String getSitemap() {
        log.info("Generating dynamic sitemap.xml");

        StringBuilder sitemap = new StringBuilder();
        sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sitemap.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // 1. Home page
        appendUrl(sitemap, frontendUrl + "/", "daily", "1.0");

        // 2. Categories
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        for (Category cat : categories) {
            appendUrl(sitemap, frontendUrl + "/category/" + cat.getSlug(), "weekly", "0.8");
        }

        // 3. Collections
        List<Collection> collections = collectionRepository.findAllByIsActiveTrue();
        for (Collection col : collections) {
            appendUrl(sitemap, frontendUrl + "/collections/" + col.getSlug(), "weekly", "0.8");
        }

        // 4. Products
        List<Product> products = productRepository.findByIsActiveTrue();
        for (Product prod : products) {
            appendUrl(sitemap, frontendUrl + "/product/" + prod.getSlug(), "weekly", "0.7");
        }

        sitemap.append("</urlset>");
        return sitemap.toString();
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getRobotsTxt() {
        log.info("Serving robots.txt");
        return """
                User-agent: *
                Allow: /
                Disallow: /admin
                Disallow: /cart
                Disallow: /checkout
                Disallow: /profile
                
                Sitemap: %s/sitemap.xml
                """.formatted(frontendUrl);
    }

    private void appendUrl(StringBuilder xml, String loc, String changefreq, String priority) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(loc).append("</loc>\n");
        xml.append("    <lastmod>").append(OffsetDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("</lastmod>\n");
        xml.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        xml.append("    <priority>").append(priority).append("</priority>\n");
        xml.append("  </url>\n");
    }
}
