package com.music.controller;

import com.music.dto.AlbumDTO;
import com.music.repository.AlbumRepository;
import com.music.model.Album;
import com.music.service.interfaces.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/albums")
@PreAuthorize("hasRole('ADMIN')")  // Ensure user has ADMIN role
@RequiredArgsConstructor
public class AlbumController {
    private final AlbumService albumService;
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
    public ResponseEntity<?> getAllAlbums(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        try {
            Page<AlbumDTO> albums = albumService.getAllAlbums(pageable);
            return ResponseEntity.ok(albums);
        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body("Error fetching albums: " + e.getMessage());
        }
    }
}
