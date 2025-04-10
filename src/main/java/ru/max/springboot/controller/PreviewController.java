package ru.max.springboot.controller;

import jakarta.validation.constraints.NotEmpty;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.max.springboot.dto.OpenGraphDataDTO;

@RestController
@RequestMapping("/api/preview")
public class PreviewController {

    @GetMapping
    public ResponseEntity<OpenGraphDataDTO> getPreview(@RequestParam @NotEmpty String url) {

        try {
            Document doc = Jsoup.connect(url).get();
            String title = doc.select("meta[property=og:title]").attr("content");
            String description = doc.select("meta[property=og:description]").attr("content");
            String image = doc.select("meta[property=og:image]").attr("content");

            OpenGraphDataDTO dataDTO = new OpenGraphDataDTO(title, url, description, image);
            return ResponseEntity.ok(dataDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
