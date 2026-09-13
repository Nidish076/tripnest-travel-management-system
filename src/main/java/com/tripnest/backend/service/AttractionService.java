package com.tripnest.backend.service;

import com.tripnest.backend.dto.request.AttractionRequest;
import com.tripnest.backend.dto.response.AttractionResponse;

import java.util.List;

public interface AttractionService {

    List<AttractionResponse> getAttractionsByDestinationId(Long destinationId);

    AttractionResponse getAttractionById(Long attractionId);

    AttractionResponse addAttraction(Long destinationId, AttractionRequest request);

    AttractionResponse updateAttraction(Long attractionId, AttractionRequest request);

    void deleteAttraction(Long attractionId);
}
