package com.Server.utils.mapper;

import com.Server.dto.OtpDTO;
import com.Server.entity.Otp;

import java.util.List;
import java.util.stream.Collectors;

public class OtpMapper {
    public static OtpDTO mapEntityToDTO(Otp otp) {
        OtpDTO otpDTO = new OtpDTO();

        otpDTO.setId(otp.getId());
        otpDTO.setCode(otp.getCode());
        otpDTO.setTimeExpired(otp.getTimeExpired());
        otpDTO.setCreatedAt(otp.getCreatedAt());
        otpDTO.setUpdatedAt(otp.getCreatedAt());

        return otpDTO;
    }

    public static List<OtpDTO> mapListEntityToListDTO(List<Otp> otpList) {
        return otpList.stream()
                .map(OtpMapper::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    public static OtpDTO mapEntityToDTOFull(Otp otp) {
        OtpDTO otpDTO = mapEntityToDTO(otp);

        if (otp.getUser() != null) {
            otpDTO.setUser(UserMapper.mapEntityToDTOFull(otp.getUser()));
        }

        return otpDTO;
    }

    public static List<OtpDTO> mapListEntityToListDTOFull(List<Otp> otpList) {
        return otpList.stream()
                .map(OtpMapper::mapEntityToDTOFull)
                .collect(Collectors.toList());
    }
}
