package com.Server.service.api;

import com.Server.dto.*;
import com.Server.entity.*;
import com.Server.exception.OurException;
import com.Server.repo.*;
import com.Server.utils.mapper.NotiMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotiApi {
    @Autowired
    private NotiRepository notiRepository;

    @Autowired
    private UserRepository userRepository;

    public Response deleteNoti(String NotiId) {
        Response response = new Response();

        try {
            notiRepository.findById(NotiId).orElseThrow(() -> new OurException("Noti Not Found"));
            notiRepository.deleteById(NotiId);

            response.setStatusCode(200);
            response.setMessage("success");
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response getAllNoti(int page, int limit, String sort, String order) {
        Response response = new Response();

        try {
            Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sort));

            Page<Noti> NotiPage = notiRepository.findAll(pageable);
            List<NotiDTO> NotiDTOList = NotiMapper.mapListEntityToListDTOFull(NotiPage.getContent());

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setPagination(new Pagination(NotiPage.getTotalElements(), NotiPage.getTotalPages(), page));
            response.setNotifications(NotiDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response getUserNotifications(String userId) {
        Response response = new Response();

        try {
            userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));

            List<Noti> userNotifications = notiRepository.findByTo(userId)
                    .stream()
                    .filter(Noti -> !Noti.getFrom().getId().equals(userId))
                    .sorted(Comparator.comparing(Noti::getCreatedAt).reversed())
                    .collect(Collectors.toList());
            List<NotiDTO> NotiDTOList = NotiMapper.mapListEntityToListDTOFull(userNotifications);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setNotifications(NotiDTOList);
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response deleteUserNotifications(String userId) {
        Response response = new Response();

        try {
            userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));
            notiRepository.deleteByFrom(userId);

            response.setStatusCode(200);
            response.setMessage("success");
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response markRead(String notiId) {
        Response response = new Response();

        try {
            Noti noti = notiRepository.findById(notiId).orElseThrow(() -> new OurException("Noti Not Found"));
            noti.setRead(true);
            notiRepository.save(noti);

            response.setStatusCode(200);
            response.setMessage("success");
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response markAllRead(String userId) {
        Response response = new Response();

        try {
            userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));
            notiRepository.markAllRead(userId);

            response.setStatusCode(200);
            response.setMessage("success");
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }
}
