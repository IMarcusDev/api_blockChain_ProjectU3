package com.proyecto.mjcd_software.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.model.entity.User;
import com.proyecto.mjcd_software.repository.UserRepository;
import com.proyecto.mjcd_software.util.HashGenerator;

@Service
public class MiningService {
    
    @Autowired
    private ConfigService configService;
    
    @Autowired
    private UserRepository userRepository;

    public Block mineBlock(Block block, String data, String userId) {
        int difficulty = configService.getDifficulty();
        String target = new String(new char[difficulty]).replace('\0', '0');
        
        long startTime = System.currentTimeMillis();
        
        while (!block.getCurrentHash().substring(0, difficulty).equals(target)) {
            block.setNonce(block.getNonce() + 1);
            String blockData = generateBlockData(block, data);
            block.setCurrentHash(HashGenerator.generateSHA256(blockData));
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("Bloque minado en: " + (endTime - startTime) + "ms");
        System.out.println("Hash encontrado: " + block.getCurrentHash());
        System.out.println("Nonce: " + block.getNonce());

        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                block.setMinedBy(userId);
                block.setMiningReward(1.0);

                user.setTotalPoints(user.getTotalPoints() + 1);
                user.setBlocksMined(user.getBlocksMined() + 1);
                userRepository.save(user);
                
                System.out.println("Puntos otorgados a usuario: " + user.getFirstName() + " " + user.getLastName());
            }
        }
        
        return block;
    }

    public Block mineBlock(Block block, String data) {
        return mineBlock(block, data, null);
    }
    
    private String generateBlockData(Block block, String content) {
        return String.format("{id:%d,hashPrevio:%s,timeStamp:%d,contenido:%s,nonce:%d}",
                block.getBlockIndex(),
                block.getPreviousHash() != null ? block.getPreviousHash() : "null",
                block.getTimestamp(),
                content,
                block.getNonce());
    }
}