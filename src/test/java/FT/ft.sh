#!/usr/bin/env bash

# 与 README.md 中的测试列表相对应

# 1. 订阅

## 1.1 订阅 qos0
mosquitto_sub -h localhost -t "#" -q 0 -v
### 1.1.1 发布 qos0
mosquitto_pub -h localhost -t "/a/b/c" -m "Hello World" -q 0
### 1.1.2 发布 qos1
mosquitto_pub -h localhost -t "/a/b/c" -m "Hello World" -q 1
### 1.1.3 发布 qos2
mosquitto_pub -h localhost -t "/a/b/c" -m "Hello World" -q 2

## 1.2 订阅 qos1
mosquitto_sub -h localhost -t "#" -q 1 -v
### 1.2.1 发布 qos0
mosquitto_pub -h localhost -t "/a/b/c" -m "Hello World" -q 0
### 1.2.2 发布 qos1
mosquitto_pub -h localhost -t "/a/b/c" -m "Hello World" -q 1
### 1.2.3 发布 qos2
mosquitto_pub -h localhost -t "/a/b/c" -m "Hello World" -q 2

## 1.3 订阅 qos2
mosquitto_sub -h localhost -t "#" -q 2 -v
### 1.3.1 发布 qos0
mosquitto_pub -h localhost -t "/a/b/c" -m "Hello World" -q 0
### 1.3.2 发布 qos1
mosquitto_pub -h localhost -t "/a/b/c" -m "Hello World" -q 1
### 1.3.3 发布 qos2
mosquitto_pub -h localhost -t "/a/b/c" -m "Hello World" -q 2

# 2. 保留会话

## 2.1 会话恢复
mosquitto_sub -h localhost -t "/a/b/c" -q 2 -u "iwant" -c -i "iwant-pc" -d
mosquitto_pub -h localhost -t "/a/b/c" -m "Hello World" -q 2
mosquitto_sub -h localhost -t "a/b/c" -q 0 -u "iwant" -c -i "iwant-pc" -d
mosquitto_pub -h localhost -t "a/b/c" -m "Hello World" -q 2
mosquitto_pub -h localhost -t "/a/b/c" -m "Hello World" -q 2

## 2.2 会话清除
mosquitto_sub -h localhost -t "/a/b/c" -q 2 -u "iwant" -c -i "iwant-pc" -d
mosquitto_sub -h localhost -t "a/b/c" -q 0 -u "iwant" -i "iwant-pc" -d
mosquitto_pub -h localhost -t "/a/b/c" -m "Hello World" -q 2

## 2.5 离线期间订阅消息发布
mosquitto_sub -h localhost -t "/a/b/c" -q 2 -u "iwant" -c -i "iwant-pc" -d
mosquitto_pub -h localhost -t "/a/b/c" -m "1" -q 0
mosquitto_pub -h localhost -t "/a/b/c" -m "2" -q 1
mosquitto_pub -h localhost -t "/a/b/c" -m "3" -q 2
mosquitto_sub -h localhost -t "/a/b/c" -q 2 -u "iwant" -c -i "iwant-pc" -d

# 3 KeepAlive

## 3.1
mosquitto_sub -h localhost -t "#" -q 0 -k 0 -d

## 3.2
mosquitto_sub -h localhost -t "#" -q 0 -k 1 -d

# 4 遗嘱

## 4.1 遗嘱发布
mosquitto_sub -h localhost -t "#" -q 0 -k 5 --will-payload "bey~" --will-qos 2 --will-topic "/a/b/c" -d
mosquitto_sub -h localhost -t "/a/b/c" -q 2 -u "iwant" -i "iwant-pc" -d

## 4.2 Disconnect 抛弃遗嘱
mosquitto_sub -h localhost -t "#" -q 0 -k 5 --will-payload "bey~" --will-qos 2 --will-topic "/a/b/c" -C 1 -d
mosquitto_sub -h localhost -t "/a/b/c" -q 2 -u "iwant" -i "iwant-pc" -d
mosquitto_pub -h localhost -t "a/b/c" -m "1" -q 0

# 5 保留消息

## 5.1 保留消息发布
mosquitto_pub -h localhost -t "/a/b/c" -m "1" -q 0 -r
mosquitto_sub -h localhost -t "/a/b/c" -q 2 -u "iwant" -i "iwant-pc" -d
mosquitto_sub -h localhost -t "/a/b/c" -q 2 -u "iwant" -i "iwant-pc" -d

## 5.2 保留消息清除
mosquitto_pub -h localhost -t "/a/b/c" -m "" -q 1 -r
mosquitto_sub -h localhost -t "/a/b/c" -q 2 -u "iwant" -i "iwant-pc" -d