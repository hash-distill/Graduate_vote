/*
 Navicat MySQL Dump SQL

 Source Server         : vote_system
 Source Server Type    : MySQL
 Source Server Version : 50731 (5.7.31-log)
 Source Host           : localhost:3306
 Source Schema         : vote

 Target Server Type    : MySQL
 Target Server Version : 50731 (5.7.31-log)
 File Encoding         : 65001

 Date: 09/09/2024 20:32:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for vote01
-- ----------------------------
DROP TABLE IF EXISTS `vote01`;
CREATE TABLE `vote01`  (
  `vote_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `vote_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '姓名',
  `vote_gender` tinyint(255) NOT NULL COMMENT '性别',
  `vote_poli` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '政治面貌',
  `vote_insti` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '学院',
  `vote_poll` tinyint(255) NULL DEFAULT 0 COMMENT '票数',
  `vote_insti_sort` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '补充：学院排序',
  `vote_inter_sort` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '补充：面试顺序',
  `vote_major` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  PRIMARY KEY (`vote_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 276 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of vote01
-- ----------------------------
INSERT INTO `vote01` VALUES (2, 'abc', 0, '共青团员', '心理学院', 5, '1/2', '12', '心理学');

-- ----------------------------
上面填写sql语句来填充你的数据库
-- ----------------------------
SET FOREIGN_KEY_CHECKS = 1;
