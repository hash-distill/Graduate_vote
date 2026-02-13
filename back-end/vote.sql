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
INSERT INTO `vote01` VALUES (220, '刘慧曦', 0, '共青团员', '心理学院', 5, '1/2', '12', '心理学');
INSERT INTO `vote01` VALUES (221, '廖乘愉', 0, '中共预备党员', '心理学院', 5, '2/2', '7', '心理学');
INSERT INTO `vote01` VALUES (222, '兰一凡', 0, '中共预备党员', '经济与管理学院', 5, '1/4', '23', '会计学');
INSERT INTO `vote01` VALUES (223, '李  玥', 0, '中共预备党员', '经济与管理学院', 5, '2/4', '46', '市场营销');
INSERT INTO `vote01` VALUES (224, '李  蓉', 0, '共青团员', '经济与管理学院', 5, '3/4', '13', '经济学');
INSERT INTO `vote01` VALUES (225, '刘佳怡', 0, '共青团员', '经济与管理学院', 5, '4/4', '27', '会计学');
INSERT INTO `vote01` VALUES (226, '李传洋', 0, '共青团员', '文学院', 5, '1/5', '29', '汉语言文学');
INSERT INTO `vote01` VALUES (227, '丛美淇', 0, '共青团员', '文学院', 5, '2/5', '52', '汉语言文学');
INSERT INTO `vote01` VALUES (228, '杨雨婷', 0, '共青团员', '文学院', 5, '3/5', '17', '汉语言文学');
INSERT INTO `vote01` VALUES (229, '吴若冰', 0, '共青团员', '文学院', 4, '4/5', '31', '汉语言文学');
INSERT INTO `vote01` VALUES (230, '杜一鸣', 0, '共青团员', '文学院', 1, '5/5', '5', '汉语言文学');
INSERT INTO `vote01` VALUES (231, '陈佳依', 0, '共青团员', '历史文化学院', 0, '1/1', '45', '历史学');
INSERT INTO `vote01` VALUES (232, '罗少谦', 0, '共青团员', '外国语学院', 0, '1/5', '18', '英语');
INSERT INTO `vote01` VALUES (233, '陈逾之', 0, '中共预备党员', '外国语学院', 0, '2/5', '49', '商务英语');
INSERT INTO `vote01` VALUES (234, '高  畅', 0, '中共预备党员', '外国语学院', 0, '3/5', '44', '商务英语');
INSERT INTO `vote01` VALUES (235, '李依依', 0, '共青团员', '外国语学院', 0, '4/5', '24', '商务英语');
INSERT INTO `vote01` VALUES (236, '于湘雨', 0, '共青团员', '外国语学院', 0, '5/5', '28', '俄语');
INSERT INTO `vote01` VALUES (237, '任奕菲', 0, '中共预备党员', '音乐学院', 0, '1/4', '43', '音乐学（钢琴，师范）');
INSERT INTO `vote01` VALUES (238, '文艺橦', 0, '中共预备党员', '音乐学院', 0, '2/4', '41', '音乐学（声乐，师范）');
INSERT INTO `vote01` VALUES (239, '郭世纪', 0, '中共预备党员', '音乐学院', 0, '3/4', '20', '舞蹈编导');
INSERT INTO `vote01` VALUES (240, '张稀越', 0, '共青团员', '音乐学院', 0, '4/4', '42', '舞蹈编导');
INSERT INTO `vote01` VALUES (241, '崔钟月', 0, '中共预备党员', '美术学院', 0, '1/5', '47', '综合材料绘画');
INSERT INTO `vote01` VALUES (242, '周亚锋', 0, '中共预备党员', '美术学院', 0, '2/5', '26', '美术学（师范)');
INSERT INTO `vote01` VALUES (243, '岳  晶', 0, '共青团员', '美术学院', 0, '3/5', '37', '数字媒介设计');
INSERT INTO `vote01` VALUES (244, '沈芷欣', 0, '中共预备党员', '美术学院', 0, '4/5', '2', '数字媒体艺术');
INSERT INTO `vote01` VALUES (245, '杜姝文', 0, '共青团员', '美术学院', 0, '5/5', '35', '视觉传达设计');
INSERT INTO `vote01` VALUES (246, '孙文绮', 0, '中共预备党员', '马克思主义学部', 0, '1/1', '4', '社会学');
INSERT INTO `vote01` VALUES (247, '姜显惠', 0, '共青团员', '数学与统计学院', 0, '1/2', '32', '数学与应用数学');
INSERT INTO `vote01` VALUES (248, '张秋红', 0, '中共预备党员', '数学与统计学院', 0, '2/2', '16', '数学与应用数学');
INSERT INTO `vote01` VALUES (249, '陈  巍', 0, '中共预备党员', '信息科学与技术学院', 0, '1/4', '19', '计算机科学与技术');
INSERT INTO `vote01` VALUES (250, '陈思佳', 0, '共青团员', '信息科学与技术学院', 0, '2/4', '40', '教育技术学');
INSERT INTO `vote01` VALUES (251, '李其繁', 0, '共青团员', '信息科学与技术学院', 0, '3/4', '56', '教育技术学');
INSERT INTO `vote01` VALUES (252, '廖俊茹', 0, '共青团员', '信息科学与技术学院', 0, '4/4', '36', '计算机科学与技术');
INSERT INTO `vote01` VALUES (253, '邵依佳', 0, '共青团员', '物理学院', 0, '1/6', '25', '物理学');
INSERT INTO `vote01` VALUES (254, '周荟晨', 0, '共青团员', '物理学院', 0, '2/6', '34', '电子信息科学与技术');
INSERT INTO `vote01` VALUES (255, '林宥涵', 0, '共青团员', '物理学院', 0, '3/6', '55', '材料物理');
INSERT INTO `vote01` VALUES (256, '郭彤宇', 0, '共青团员', '物理学院', 0, '4/6', '51', '电子信息科学与技术');
INSERT INTO `vote01` VALUES (257, '马  雪', 0, '共青团员', '物理学院', 0, '5/6', '10', '材料物理');
INSERT INTO `vote01` VALUES (258, '纪丁豪', 0, '共青团员', '物理学院', 0, '6/6', '30', '物理学');
INSERT INTO `vote01` VALUES (259, '赵欣颖', 0, '共青团员', '化学学院', 0, '1/3', '48', '化学');
INSERT INTO `vote01` VALUES (260, '李佳茗', 0, '共青团员', '化学学院', 0, '2/3', '22', '化学');
INSERT INTO `vote01` VALUES (261, '王莉雯', 0, '共青团员', '化学学院', 0, '3/3', '53', '化学');
INSERT INTO `vote01` VALUES (262, '王舒逸', 0, '中共预备党员', '生命科学学院', 0, '1/5', '15', '生物技术');
INSERT INTO `vote01` VALUES (263, '杨琦羽', 0, '共青团员', '生命科学学院', 0, '2/5', '50', '生物科学');
INSERT INTO `vote01` VALUES (264, '才昕妤', 0, '中共预备党员', '生命科学学院', 0, '3/5', '9', '生物科学');
INSERT INTO `vote01` VALUES (265, '崔涵雅', 0, '共青团员', '生命科学学院', 0, '4/5', '21', '生物科学');
INSERT INTO `vote01` VALUES (266, '王艺欣', 0, '共青团员', '生命科学学院', 0, '5/5', '1', '生物技术');
INSERT INTO `vote01` VALUES (267, '王一喆', 0, '中共预备党员', '地理科学学院', 0, '1/4', '14', '人文地理与城乡规划');
INSERT INTO `vote01` VALUES (268, '左一景', 0, '共青团员', '地理科学学院', 0, '2/4', '11', '人文地理与城乡规划');
INSERT INTO `vote01` VALUES (269, '谭棋月', 0, '共青团员', '地理科学学院', 0, '3/4', '33', '地理信息科学');
INSERT INTO `vote01` VALUES (270, '林雪埜', 0, '共青团员', '地理科学学院', 0, '4/4', '6', '人文地理与城乡规划');
INSERT INTO `vote01` VALUES (271, '刘禹彤', 0, '中共党员', '体育学院', 0, '1/2', '39', '运动训练');
INSERT INTO `vote01` VALUES (272, '许  琦', 0, '共青团员', '体育学院', 0, '2/2', '3', '运动训练');
INSERT INTO `vote01` VALUES (273, '李  悦', 0, '中共预备党员', '传媒科学学院（新闻学院）', 0, '1/3', '54', '广播电视编导');
INSERT INTO `vote01` VALUES (274, '屠杭莹', 0, '共青团员', '传媒科学学院（新闻学院）', 0, '2/3', '38', '新闻学');
INSERT INTO `vote01` VALUES (275, '孙梦宇', 0, '共青团员', '传媒科学学院（新闻学院）', 0, '3/3', '8', '数字媒体技术');

SET FOREIGN_KEY_CHECKS = 1;
