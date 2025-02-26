package com.gamego.service;


import com.gamego.domain.account.Account;
import com.gamego.domain.review.Review;
import com.gamego.domain.review.form.ReviewForm;
import com.gamego.domain.room.Room;
import com.gamego.repository.ReviewRepository;
import com.gamego.repository.RoomRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;


    public void addReview(String path, Account account, ReviewForm reviewForm) {
        Room room = roomRepository.findByPath(path);
        if (room == null) {
            throw new IllegalArgumentException("방이 존재하지 않습니다.");
        }

        Review review = Review.builder()
                .account(account)
                .room(room)
                .content(reviewForm.getContent())
                .createdAt(LocalDateTime.now())
                .rating(reviewForm.getRating())
                .build();

        reviewRepository.save(review);
    }

    public void updateReview(Room room, Account account, ReviewForm reviewForm) {
        Review review = reviewRepository.findByRoomAndAccount(room, account).orElseThrow(() -> new IllegalArgumentException("해당 리뷰가 존재하지 않습니다."));
        review.changeRating(reviewForm.getRating());
        review.changeContent(reviewForm.getContent());
    }

    public void deleteReview(Room room, Account account) {
        Review review = reviewRepository.findByRoomAndAccount(room, account).orElseThrow(() -> new IllegalArgumentException("해당 리뷰가 존재하지 않습니다."));
        reviewRepository.delete(review);
    }
}
