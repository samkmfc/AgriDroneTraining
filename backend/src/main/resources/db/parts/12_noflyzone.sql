USE agri_drone_training;

INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('北京首都国际机场', '北京市', '北京市', 116.597504, 40.072499, 8000, 'STRICT', '机场净空保护区，区域内严禁任何无人机起降与飞行，违者依法查处。');
INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('北京大兴国际机场', '北京市', '北京市', 116.410880, 39.509167, 9000, 'STRICT', '机场净空保护区，禁止一切无人机活动，确保民航航班安全起降。');
INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('上海浦东国际机场', '上海市', '上海市', 121.805214, 31.142363, 9000, 'STRICT', '机场净空保护区，严禁任何无人机飞行，违规将影响航班安全运行。');
INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('上海虹桥国际机场', '上海市', '上海市', 121.336310, 31.198056, 8000, 'STRICT', '机场净空保护区，禁止无人机起降及穿越，保障繁忙空域安全。');
INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('广州白云国际机场', '广东省', '广州市', 113.298786, 23.392436, 9000, 'STRICT', '机场净空保护区，区域内全面禁飞，严禁任何无人机靠近航道。');
INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('深圳宝安国际机场', '广东省', '深圳市', 113.810664, 22.639279, 8000, 'STRICT', '机场净空保护区，严禁无人机飞行，确保航班起降绝对安全。');
INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('成都双流国际机场', '四川省', '成都市', 103.955383, 30.578528, 8000, 'STRICT', '机场净空保护区，禁止任何无人机活动，保护民航飞行安全。');
INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('重庆江北国际机场', '重庆市', '重庆市', 106.638023, 29.719279, 8000, 'STRICT', '机场净空保护区，严禁无人机进入飞行，违者依法依规处理。');
INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('西安咸阳国际机场', '陕西省', '西安市', 108.751592, 34.447119, 8000, 'STRICT', '机场净空保护区，区域内禁止一切无人机起降与穿越飞行。');
INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('杭州萧山国际机场', '浙江省', '杭州市', 120.434453, 30.234191, 8000, 'STRICT', '机场净空保护区，严禁任何无人机飞行，保障航班安全运行。');
INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('武汉天河国际机场', '湖北省', '武汉市', 114.207375, 30.776906, 8000, 'STRICT', '机场净空保护区，禁止无人机进入该空域，确保民航起降安全。');
INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('郑州新郑国际机场', '河南省', '郑州市', 113.842562, 34.524739, 8000, 'STRICT', '机场净空保护区，区域内全面禁飞，严禁无人机靠近跑道航道。');
INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('珠海金湾机场', '广东省', '珠海市', 113.375825, 22.011580, 7000, 'STRICT', '机场净空保护区，严禁任何无人机飞行，保障航班安全起降。');
INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('北京天安门重点区域', '北京市', '北京市', 116.397455, 39.908823, 3000, 'STRICT', '城市核心重点保护区域，禁止任何无人机飞行，属永久禁飞范围。');
INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('上海陆家嘴金融城限飞区', '上海市', '上海市', 121.505220, 31.245480, 4000, 'LIMIT', '城市中心人口密集限飞区，无人机飞行须提前申报并经批准方可执行。');
INSERT INTO no_fly_zone (name, province, city, longitude, latitude, radius, level, description) VALUES ('某军事管理区周边限飞区', '河北省', '保定市', 115.464589, 38.873891, 5000, 'LIMIT', '军事管理区周边限飞区域，无人机活动须依法申报审批后方可飞行。');
