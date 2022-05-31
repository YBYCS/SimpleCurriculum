# SimpleCurriculum
仿小爱课程表制作的一款课程表  
该有的功能都具备了 因为砍掉了一些我个人认为不怎么用得到的功能 所以响应速度会快很多  
本人只学了差不多一周的android开发所以代码可能不太优美(所以也不会很难)  
推荐阅读顺序是 设置 课程表 今日课程 每一段代码我差不多都注释了 应该都挺好理解的
# TODO
1. 联网判断今天要不要补课或者是休息
2. 添加更多颜色选择
3. 课程展示改为滑动(其实一开始以为这个是小爱同学响应慢的原因所以改了 但是判断可能不是 而是网络连接? 代考证)
4. 数据合法性验证
# 后期修复
1. 修复了颜色位置问题
2. 修复了修改学期周数后导致的数组越界问题并且添加了自动补充课程的功能
3. 修复了课表节数相关bug
# 一些解释
## sqlite相关数据
数据库名称:data.db
有三个表
### 设置表
名称setting 两列是name和state加上id 以下全为name 对应一state
//开学时间
year
month
day
//目前第几周
nowweek
//是否展示周末
isshowweekend
//学期一共多少周
weeksum
//课表节数设置
msum
asum
esum
//每节课是否为固定时间
isfixedtime
courseduration一节课时间
restduration课间时间

### 课程时间设置表

courseschedule 同上 name 和state

1. 上下晚 period 分别用 int类型的123代表是什么时候 然后接上1234作为第几节课 两位数

2. 开始时间小时 beginhour

3. 开始时间分钟 beginminute

4. 结束时间 endhour endminute

### 课程数据表

coursedata 以下是各列名称

name课程名称

teacher老师名称

classroom教室

begintime第几节开始 每个时间段都从1开始

sum一共多少节课

week字符串对应

color展示颜色

period 时期编码  两位数 分别对应星期几和时间段

isOddWeek是否为单周(调整周数时自动补全)

isDoubleWeek是否为双周(全选就是单双周都为1)

# 效果展示
![image](https://github.com/YBYCS/SimpleCurriculum/blob/master/images/today.jpg)  
![image](https://github.com/YBYCS/SimpleCurriculum/blob/master/images/kcb.jpg)  
![image](https://github.com/YBYCS/SimpleCurriculum/blob/master/images/setting.jpg)  
![image](https://github.com/YBYCS/SimpleCurriculum/blob/master/images/setting2.jpg)  
