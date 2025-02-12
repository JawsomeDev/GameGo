package com.gamego.service;

import com.gamego.domain.room.Room;
import com.gamego.domain.room.dto.RoomResp;
import com.gamego.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomQueryService {

    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    public RoomResp getRoom(String path){
        Room room = roomRepository.findByPath(path);
        if (room == null) {
            throw new IllegalArgumentException(path + "에 해당하는 방이 없습니다.");
        }
        return modelMapper.map(room, RoomResp.class);
    }
}
