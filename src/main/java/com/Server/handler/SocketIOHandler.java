package com.Server.handler;

import com.Server.dto.*;
import com.Server.repo.*;
import com.Server.entity.*;
import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.DataListener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import org.springframework.stereotype.Component;

class MapData extends HashMap<String, Object> {
  private static final long serialVersionUID = 1L;
}

@Component
public class SocketIOHandler {

  private final SocketIOServer server;
  private final ConcurrentHashMap<String, SocketIOClient> userSocketMap = new ConcurrentHashMap<>();
  private final NotiRepository notificationRepository;

  public SocketIOHandler(SocketIOServer server, NotiRepository notificationRepository) {
    this.server = server;
    this.notificationRepository = notificationRepository;
  }

  public ConcurrentHashMap<String, SocketIOClient> getUserSocketMap() {
    return userSocketMap;
  }

  public List<String> getOnlineUserIds() {
    return new ArrayList<>(userSocketMap.keySet());
  }

  public void sendNotification(String userId, Object notificationDTO) {
    SocketIOClient client = userSocketMap.get(userId);
    if (client != null && client.isChannelOpen()) {
      client.sendEvent("notification", notificationDTO);
      System.out.println("✅ Sent notification to user: " + userId);
    } else {
      System.out.println("❌ Cannot send notification: User " + userId + " not connected.");
    }
  }

  private void broadcastOnlineUsersList() {
    List<String> onlineUsers = getOnlineUserIds();
    Map<String, Object> onlineUsersData = new HashMap<>();
    onlineUsersData.put("onlineUsers", onlineUsers);

    for (SocketIOClient client : server.getAllClients()) {
      client.sendEvent("onlineUsers", onlineUsersData);
    }

    System.out.println("📣 Broadcast online users list: " + onlineUsers.size() + " users");
  }

  public boolean logUserLoginAndConnection(String userId, Map<String, String> userInfo) {
    boolean isConnected = userSocketMap.containsKey(userId);

    // System.out.println("\n🔄 ĐỒNG BỘ THÔNG TIN ĐĂNG NHẬP VÀ KẾT NỐI WEBSOCKET ---------------");
    System.out.println("👤 User ID: " + userId);

    if (userInfo != null) {
      if (userInfo.containsKey("email")) {
        System.out.println("📧 Email: " + userInfo.get("email"));
      }
      if (userInfo.containsKey("fullName")) {
        System.out.println("👨‍💼 Họ và tên: " + userInfo.get("fullName"));
      }
    }

    // System.out.println("📱 Trạng thái kết nối WebSocket: " + (isConnected ? "ĐÃ KẾT NỐI ✅" : "CHƯA KẾT NỐI ❌"));

    if (isConnected) {
      SocketIOClient client = userSocketMap.get(userId);
      System.out.println("🔌 Session ID: " + client.getSessionId());
      System.out.println("🌐 Địa chỉ IP: " + client.getHandshakeData().getAddress());
    }

    // System.out.println("🕒 Thời gian kiểm tra: " + LocalDateTime.now());
    // System.out.println("📊 Tổng số người dùng online: " + userSocketMap.size());
    System.out.println("------------------------------------------------------------------\n");

    return isConnected;
  }

  @PostConstruct
  public void startServer() {
    server.addConnectListener(client -> {
      String userId = client.getHandshakeData().getSingleUrlParam("userId");
      System.out.println(
          "🔍 Handshake data: " + client.getHandshakeData().getUrl());

      if (userId != null) {
        // Lưu kết nối WebSocket vào map
        userSocketMap.put(userId, client);
        client.sendEvent("me", userId);

        // Log thông tin chi tiết về người dùng đăng nhập
        // System.out.println("✅ User connected: " + userId);
        // System.out.println("🕒 Thời gian kết nối: " + LocalDateTime.now());
        // System.out.println("🔌 SessionId: " + client.getSessionId());
        // System.out.println("🌐 Địa chỉ IP: " + client.getHandshakeData().getAddress());
        // System.out.println("📊 Current connected users: " + userSocketMap.keySet());
        // System.out.println("📊 Tổng số người dùng online: " + userSocketMap.size());

        // Gửi sự kiện userConnected cho tất cả client
        Map<String, Object> userConnectedData = new HashMap<>();
        userConnectedData.put("userId", userId);
        userConnectedData.put("connectTime", LocalDateTime.now().toString());
        server.getBroadcastOperations().sendEvent("userConnected", userConnectedData);

        // Gửi danh sách người dùng đang online
        broadcastOnlineUsersList();
      } else {
        System.out.println("⚠️ User ID is null on connection!");
      }
    });

    server.addDisconnectListener(client -> {
      // Find and remove the user by client reference
      String userIdToRemove = null;
      for (Map.Entry<String, SocketIOClient> entry : userSocketMap.entrySet()) {
        if (entry.getValue().equals(client)) {
          userIdToRemove = entry.getKey();
          break;
        }
      }

      if (userIdToRemove != null) {
        userSocketMap.remove(userIdToRemove);
        System.out.println("❌ User disconnected: " + userIdToRemove);
        System.out.println(
            "📊 Remaining connected users: " + userSocketMap.keySet());

        // Gửi sự kiện userDisconnected cho tất cả client
        Map<String, Object> userDisconnectedData = new HashMap<>();
        userDisconnectedData.put("userId", userIdToRemove);
        server.getBroadcastOperations().sendEvent("userDisconnected", userDisconnectedData);

        // Gửi danh sách người dùng đang online
        broadcastOnlineUsersList();
      } else {
        System.out.println(
            "❌ Client disconnected: " +
                client.getSessionId() +
                " (not found in user map)");
      }
    });

    // Thêm sự kiện get-online-users để client có thể lấy danh sách người dùng đang
    // online
    server.addEventListener("getOnlineUsers", Object.class, (client, data, ackSender) -> {
      List<String> onlineUsers = getOnlineUserIds();
      Map<String, Object> response = new HashMap<>();
      response.put("onlineUsers", onlineUsers);

      client.sendEvent("onlineUsers", response);
      System.out.println("📤 Sent online users list to client: " + onlineUsers.size() + " users");
    });

    server.addEventListener("notification", NotiDTO.class, (client, data, ackSender) -> {
      System.out
          .println("📢 Nhận sự kiện notification cho user: " + data.getTo() + ", nội dung: " + data.getType());
    });

    System.out.println("🚀 Socket.IO Handler started!");

    server.addEventListener(
        "callUser",
        CallDataDTO.class,
        new DataListener<CallDataDTO>() {
          @Override
          public void onData(
              SocketIOClient client,
              CallDataDTO data,
              AckRequest ackSender) {
            // System.out.println("📥 Nhận yêu cầu gọi:");
            // System.out.println(" - Từ user: " + data.getFrom());
            // System.out.println(" - Gọi đến user: " + data.getUserToCall());
            // System.out.println(" - Dữ liệu signal: " + data.getSignalData());
            // System.out.println(" - Tên người gọi: " + data.getName());

            // Validate signal data
            if (data.getSignalData() == null) {
              System.out.println("❌ Signal data is null!");
              return;
            }

            SocketIOClient receiver = userSocketMap.get(data.getUserToCall());
            if (receiver != null) {
              System.out.println(
                  "📞 Đang chuyển tiếp cuộc gọi đến " + data.getUserToCall());

              // Create a Map with the necessary fields
              Map<String, Object> callData = new HashMap<>();
              callData.put("from", data.getFrom());
              callData.put("signal", data.getSignalData());
              callData.put("name", data.getName());

              // Send the data as a Map
              receiver.sendEvent("callUser", callData);

              System.out.println("✅ Đã chuyển tiếp cuộc gọi thành công");
            } else {
              System.out.println(
                  "❌ Không tìm thấy user " +
                      data.getUserToCall() +
                      " trong danh sách kết nối!");
              System.out.println(
                  "📊 Danh sách user đang kết nối: " + userSocketMap.keySet());
            }
          }
        });

    server.addEventListener(
        "answerCall",
        AnswerDataDTO.class,
        new DataListener<AnswerDataDTO>() {
          @Override
          public void onData(
              SocketIOClient client,
              AnswerDataDTO data,
              AckRequest ackSender) {
            // Convert Long to String for consistent key lookup
            String toUserId = String.valueOf(data.getTo());
            // System.out.println("📥 Nhận trả lời cuộc gọi:");
            // System.out.println(" - Từ user: " + client.getSessionId());
            // System.out.println(" - Gửi đến user: " + toUserId);
            // System.out.println(" - Dữ liệu signal: " + data.getSignal());

            SocketIOClient caller = userSocketMap.get(toUserId);
            if (caller != null) {
              System.out.println(
                  "✅ Answer received from " +
                      client.getSessionId() +
                      " forwarding to " +
                      toUserId);
              // Send the signal data directly
              caller.sendEvent("callAccepted", data.getSignal());
              System.out.println("✅ Đã chuyển tiếp trả lời thành công");
            } else {
              System.out.println("❌ Caller " + toUserId + " is not connected!");
              System.out.println(
                  "📊 Danh sách user đang kết nối: " + userSocketMap.keySet());
            }
          }
        });
    server.addEventListener("endCall", CallDataDTO.class, new DataListener<CallDataDTO>() {
      @Override
      public void onData(SocketIOClient client, CallDataDTO data, AckRequest ackSender) {
        System.out.println("📞 Nhận yêu cầu kết thúc cuộc gọi từ user: " + data.getUserId());
        System.out.println("📞 Call ID: " + data.getCallId());

        SocketIOClient receiver = userSocketMap.get(data.getUserId());
        if (receiver != null) {
          System.out.println("📞 Đang thông báo kết thúc cuộc gọi đến user: " + data.getUserId());
          receiver.sendEvent("callEnded", data.getCallId());
        } else {
          System.out.println("❌ Không tìm thấy user " + data.getUserId() + " trong danh sách kết nối!");
        }
      }
    });

    // Handle ICE candidates
    server.addEventListener(
        "iceCandidate",
        MapData.class,
        new DataListener<MapData>() {
          @Override
          public void onData(
              SocketIOClient client,
              MapData data,
              AckRequest ackSender) {
            String to = (String) data.get("to");
            SocketIOClient receiver = userSocketMap.get(to);
            if (receiver != null) {
              System.out.println("🧊 Forwarding ICE candidate to: " + to);
              // Send the candidate data directly
              receiver.sendEvent("iceCandidate", data.get("candidate"));
            } else {
              System.out.println(
                  "❌ Receiver " + to + " not found for ICE candidate!");
            }
          }
        });

    server.addEventListener("followNotification", MapData.class, new DataListener<MapData>() {
      @Override
      public void onData(SocketIOClient client, MapData data, AckRequest ackSender) {
        try {
          String fromUserId = (String) data.get("fromUserId");
          String fromUserName = (String) data.get("fromUserName");
          String toUserId = (String) data.get("toUserId");
          String message = (String) data.get("message");
          String timestamp = (String) data.get("timestamp");

          System.out.println("📥 Follow event received:");
          System.out.println(" - From: " + fromUserName + " (ID: " + fromUserId + ")");
          System.out.println(" - To: " + toUserId);
          System.out.println(" - Message: " + message);
          System.out.println(" - Time: " + timestamp);

          LocalDateTime sentAt;
          try {
            sentAt = (timestamp != null && !timestamp.isEmpty())
                ? LocalDateTime.parse(timestamp)
                : LocalDateTime.now();
          } catch (Exception e) {
            sentAt = LocalDateTime.now();
          }

          SocketIOClient receiver = userSocketMap.get(toUserId);
          if (receiver != null) {
            Map<String, Object> notifyData = new HashMap<>();
            notifyData.put("fromUserId", fromUserId);
            notifyData.put("fromUserName", fromUserName);
            notifyData.put("message", message);
            notifyData.put("sentAt", sentAt.toString());

            // Tạo các đối tượng liên quan
            User fromUser = new User();
            fromUser.setId(fromUserId);

            User toUser = new User();
            toUser.setId(toUserId);

            Noti notification = new Noti(Noti.TYPE.FOLLOW, fromUser, toUser);
            notificationRepository.save(notification);

            receiver.sendEvent("receiveFollowNotification", notifyData);

            System.out.println("✅ Follow notification sent to " + toUserId);
          } else {
            System.out.println("❌ User " + toUserId + " is not connected.");
          }
        } catch (Exception ex) {
          ex.printStackTrace();
          System.out.println("❌ Error processing follow notification: " + ex.getMessage());
        }
      }
    });
    System.out.println("🚀 Socket.IO Handler started!");
  }

  @PreDestroy
  public void stopServer() {
    server.stop();
    System.out.println("❌ Socket.IO Server stopped.");
  }
}
