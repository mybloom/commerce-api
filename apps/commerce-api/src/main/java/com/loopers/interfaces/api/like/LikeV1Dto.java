package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeResult;
import com.loopers.domain.like.LikeQuery;

public class LikeV1Dto {

    public record RegisterResponse(
            boolean isDuplicatedRequest
    ) {
        public static RegisterResponse from(LikeResult.LikeRegisterResult result) {
            return new RegisterResponse(
                    result.isDuplicatedRequest()
            );
        }

        public static RegisterResponse from(LikeQuery.LikeRegisterQuery query) {
            return new RegisterResponse(
                    query.isDuplicatedRequest()
            );
        }
    }

    public record RemoveResponse(
            boolean isDuplicatedRequest
    ) {
        public static RemoveResponse from(LikeResult.LikeRemoveResult result) {
            return new RemoveResponse(
                    result.isDuplicatedRequest()
            );
        }

        public static RemoveResponse from(LikeQuery.LikeRemoveQuery query) {
            return new RemoveResponse(
                    query.isDuplicatedRequest()
            );
        }
    }
}
