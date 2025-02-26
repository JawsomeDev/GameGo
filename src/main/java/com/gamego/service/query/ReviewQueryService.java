package com.gamego.service.query;


import com.gamego.domain.account.Account;
import com.gamego.domain.review.Review;
import com.gamego.domain.room.Room;
import com.gamego.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;

    public Page<Review> getReviews(Long roomId, Pageable pageable) {
        return reviewRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable);
    }

    public Double calculateAverageRating(Page<Review> reviews) {
        if(reviews == null || reviews.isEmpty()) {
            return null;
        }
        double sum = 0;
        int count = 0;
        for(Review review : reviews) {
            if(review.getRating() !=null){
                sum += review.getRating();
                count++;
            }
        }
        return (count == 0) ? null : sum/count;
    }

    public boolean findByRoomAndAccount(Room room, Account account){
        Optional<Review> existingReview = reviewRepository.findByRoomAndAccount(room, account);
        if (existingReview.isPresent()) {
            return true;
        }
        return false;
    }
}
