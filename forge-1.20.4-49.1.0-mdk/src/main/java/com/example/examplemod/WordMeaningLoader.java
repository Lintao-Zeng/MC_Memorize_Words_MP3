package com.example.examplemod;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;

public class WordMeaningLoader {
    private final List<Map.Entry<String, String>> wordList = new ArrayList<>();
    private int currentIndex = 0;

    // 从文件中加载单词和释义
    public void loadWordsFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // 跳过空行
                }
                String[] parts = line.split("@");
                if (parts.length == 2) {
                    wordList.add(new AbstractMap.SimpleEntry<>(parts[0].trim(), parts[1].trim()));
                }
            }
        } catch (IOException e) {
            LOGGER.error("加载单词文件时出错: {}", e.getMessage(), e);
        }
    }

    // 获取下一个单词（按顺序循环）
    public Map.Entry<String, String> getNextWord() {
        if (wordList.isEmpty()) {
            return null; // 如果列表为空，返回 null
        }

        Map.Entry<String, String> word = wordList.get(currentIndex); // 获取当前索引的单词
        currentIndex = (currentIndex + 1) % wordList.size(); // 增加索引，并循环
        return word;
    }
}
