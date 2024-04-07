package cf.vsing.community.util;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveWorldUtil {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWorldUtil.class);
    private static final Character REPLACED = '*';
    private final TrieNode rootTrie = new TrieNode();


    private void addWord(String word) {
        TrieNode temp = rootTrie;
        for (int i = 0; i < word.length(); i++) {
            Character character = word.charAt(i);
            TrieNode subNode = temp.getSubNode(character);
            if (subNode == null) {
                subNode = new TrieNode(character, i == word.length() - 1);
                temp.addSubNode(subNode);
            }
            temp = subNode;
        }
    }

    @PostConstruct
    public void init() {
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitiveWords.txt");
            if (is == null) {
                throw new IOException();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String word;
            while ((word = reader.readLine()) != null) {
                this.addWord(word);
            }
            is.close();
        } catch (IOException e) {
            log.error("读取敏感词文件错误：" + e.getMessage());
        }
    }

    public String filter(String text) {
        //空值处理
        if (StringUtils.isBlank(text)) {
            return null;
        }

        TrieNode trieNodeP = rootTrie;
        int fastP = 0, slowP = 0;
        StringBuilder sb = new StringBuilder();
        //过滤敏感词
        while (fastP < text.length()) {
            Character c = text.charAt(fastP);
            //判断特殊字符
            if (isSymbol(c)) {
                if (trieNodeP == rootTrie) {
                    sb.append(c);
                    slowP++;
                }
                fastP++;
                continue;
            }
            //判断快指针字符与前缀树指针字符
            trieNodeP = trieNodeP.getSubNode(c);
            if (trieNodeP == null) {
                sb.append(text.charAt(slowP));
                fastP = ++slowP;
                trieNodeP = rootTrie;
            } else if (trieNodeP.isLeaf()) {
                sb.append(StringUtils.repeat(REPLACED, fastP - slowP + 1));
                slowP = ++fastP;
                trieNodeP = rootTrie;
            } else {
                fastP++;
            }
        }
        sb.append(text.substring(slowP));
        return sb.toString();
    }

    public boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    private class TrieNode {
        private Character character;
        private boolean isLeaf = false;
        Map<Character, TrieNode> subNode = new HashMap<>();

        public TrieNode() {
        }

        public TrieNode(Character character, boolean isLeaf) {
            this.character = character;
            this.isLeaf = isLeaf;
        }

        public void addSubNode(TrieNode node) {
            subNode.put(node.getCharacter(), node);
        }

        public Character getCharacter() {
            return character;
        }

        public boolean isLeaf() {
            return isLeaf;
        }

        public TrieNode getSubNode(Character character) {
            if (subNode == null) {
                return null;
            }
            return subNode.get(character);
        }

    }

}
