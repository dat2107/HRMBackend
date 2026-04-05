package com.toto.backend.service;

import com.toto.backend.entity.UpdateRequest;

import java.util.List;

public interface AdminService {
    List<UpdateRequest> getRequests(String statusFilter);
    void approveRequest(Long requestId, String adminId);
    void rejectRequest(Long requestId, String adminId, String adminNote);
}
