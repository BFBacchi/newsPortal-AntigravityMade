package com.newsportal.repository;

import com.newsportal.model.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {

    List<MediaAsset> findByNewsIdOrderByDisplayOrderAsc(Long newsId);

    List<MediaAsset> findByNewsIdAndType(Long newsId, MediaAsset.MediaType type);

    List<MediaAsset> findByAiGeneratedTrue();
}
