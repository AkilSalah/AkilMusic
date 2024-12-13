package com.music.controller;

import com.music.repository.AlbumRepository;
import com.music.model.Album;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/albums")
@PreAuthorize("hasRole('ADMIN')")  // Ensure user has ADMIN role
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumRepository albumRepository;

    @PostMapping
    public ResponseEntity<?> createAlbum(@RequestBody Album album) {
        try {
            Album savedAlbum = albumRepository.save(album);
            return ResponseEntity.ok(savedAlbum);
        } catch (Exception e) {
            return ResponseEntity
                .internalServerError()
                .body("Error creating album: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllAlbums() {
        try {
            return ResponseEntity.ok(albumRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity
                .internalServerError()
                .body("Error fetching albums: " + e.getMessage());
        }
    }
}
