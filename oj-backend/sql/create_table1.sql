# 数据库初始化
# @author <a href="">程序员阿文</a>
# @from <a href="">在线编程系统</a>

-- 创建库
CREATE DATABASE IF NOT EXISTS my_db;

-- 切换库
USE my_db;

-- 用户表
CREATE TABLE IF NOT EXISTS user
(
    id           BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    userAccount  VARCHAR(256)                           NOT NULL COMMENT '账号',
    userPassword VARCHAR(512)                           NOT NULL COMMENT '密码',
    unionId      VARCHAR(256)                           NULL COMMENT '微信开放平台id',
    mpOpenId     VARCHAR(256)                           NULL COMMENT '公众号openId',
    userName     VARCHAR(256)                           NULL COMMENT '用户昵称',
    userAvatar   VARCHAR(1024)                          NULL COMMENT '用户头像',
    userProfile  VARCHAR(512)                           NULL COMMENT '用户简介',
    userRole     VARCHAR(256) DEFAULT 'user'            NOT NULL COMMENT '用户角色：user/admin/ban',
    createTime   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete     TINYINT      DEFAULT 0                 NOT NULL COMMENT '是否删除',
    INDEX idx_unionId (unionId)
) COMMENT '用户' COLLATE = utf8mb4_unicode_ci;

-- 题目表
CREATE TABLE IF NOT EXISTS question
(
    id          BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    title       VARCHAR(512)                       NULL COMMENT '标题',
    content     TEXT                               NULL COMMENT '内容',
    tags        VARCHAR(1024)                      NULL COMMENT '标签列表（json 数组）',
    answer      TEXT                               NULL COMMENT '题目答案',
    submitNum   INT      DEFAULT 0                 NOT NULL COMMENT '题目提交数',
    acceptedNum INT      DEFAULT 0                 NOT NULL COMMENT '题目通过数',
    judgeCase   TEXT                               NULL COMMENT '判题用例（json 数组）',
    judgeConfig TEXT                               NULL COMMENT '判题配置（json 对象）',
    thumbNum    INT      DEFAULT 0                 NOT NULL COMMENT '点赞数',
    favourNum   INT      DEFAULT 0                 NOT NULL COMMENT '收藏数',
    userId      BIGINT                             NOT NULL COMMENT '创建用户 id',
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete    TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    INDEX idx_userId (userId),
    CONSTRAINT fk_question_user FOREIGN KEY (userId) REFERENCES user (id)
) COMMENT '题目' COLLATE = utf8mb4_unicode_ci;

-- 题目提交表
CREATE TABLE IF NOT EXISTS question_submit
(
    id         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    language   VARCHAR(128)                       NOT NULL COMMENT '编程语言',
    code       TEXT                               NOT NULL COMMENT '用户代码',
    judgeInfo  TEXT                               NULL COMMENT '判题信息（json 对象）',
    status     INT      DEFAULT 0                 NOT NULL COMMENT '判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）',
    questionId BIGINT                             NOT NULL COMMENT '题目 id',
    userId     BIGINT                             NOT NULL COMMENT '创建用户 id',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete   TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    INDEX idx_questionId (questionId),
    INDEX idx_userId (userId),
    CONSTRAINT fk_submit_question FOREIGN KEY (questionId) REFERENCES question (id),
    CONSTRAINT fk_submit_user FOREIGN KEY (userId) REFERENCES user (id)
) COMMENT '题目提交' COLLATE = utf8mb4_unicode_ci;

-- 帖子表
CREATE TABLE IF NOT EXISTS post
(
    id         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    title      VARCHAR(512)                       NULL COMMENT '标题',
    content    TEXT                               NULL COMMENT '内容',
    tags       VARCHAR(1024)                      NULL COMMENT '标签列表（json 数组）',
    thumbNum   INT      DEFAULT 0                 NOT NULL COMMENT '点赞数',
    favourNum  INT      DEFAULT 0                 NOT NULL COMMENT '收藏数',
    userId     BIGINT                             NOT NULL COMMENT '创建用户 id',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete   TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    INDEX idx_userId (userId),
    CONSTRAINT fk_post_user FOREIGN KEY (userId) REFERENCES user (id)
) COMMENT '帖子' COLLATE = utf8mb4_unicode_ci;

-- 帖子点赞表（硬删除）
CREATE TABLE IF NOT EXISTS post_thumb
(
    id         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    postId     BIGINT                             NOT NULL COMMENT '帖子 id',
    userId     BIGINT                             NOT NULL COMMENT '创建用户 id',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_postId (postId),
    INDEX idx_userId (userId),
    CONSTRAINT fk_thumb_post FOREIGN KEY (postId) REFERENCES post (id),
    CONSTRAINT fk_thumb_user FOREIGN KEY (userId) REFERENCES user (id)
) COMMENT '帖子点赞';

-- 帖子收藏表（硬删除）
CREATE TABLE IF NOT EXISTS post_favour
(
    id         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    postId     BIGINT                             NOT NULL COMMENT '帖子 id',
    userId     BIGINT                             NOT NULL COMMENT '创建用户 id',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_postId (postId),
    INDEX idx_userId (userId),
    CONSTRAINT fk_favour_post FOREIGN KEY (postId) REFERENCES post (id),
    CONSTRAINT fk_favour_user FOREIGN KEY (userId) REFERENCES user (id)
) COMMENT '帖子收藏';



