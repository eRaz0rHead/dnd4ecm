DROP TABLE IF EXISTS ITEM;
CREATE TABLE ITEM(
	 ID INT PRIMARY KEY,
	 name VARCHAR(100),
	 lvl int,
	 cost VARCHAR(20),
	 itype VARCHAR(50),
	 subtypes VARCHAR(50),
	 bonus VARCHAR(50),
	 enhances VARCHAR(50),
	 critical  VARCHAR(200),
	 property VARCHAR(1500),
	 powers CLOB
);
-- ;name :lvl :type :usage :kwords :action :target :range :source :desc
DROP TABLE IF EXISTS POWER;
CREATE TABLE POWER(
	 ID INT PRIMARY KEY,
	 name VARCHAR(100),
	 lvl int,
	 itype VARCHAR(50),
	 usage VARCHAR(50),
	 kwords VARCHAR(50),
	 action VARCHAR(50),
	 target VARCHAR(50),
	 range VARCHAR(50),
	 source VARCHAR(50),
	 description CLOB
);

DROP TABLE IF EXISTS CHARCLASS;
CREATE TABLE CHARCLASS(
	 ID INT PRIMARY KEY,
	 name VARCHAR(50),
	 description CLOB
);
--:name :tier :prereq :benefit
DROP TABLE IF EXISTS FEAT;
CREATE TABLE FEAT(
	 ID INT PRIMARY KEY,
	 name VARCHAR(50),
	 tier VARCHAR(50),
	 prereq VARCHAR(50),
	 benefit VARCHAR(1000)
);


DROP TABLE IF EXISTS VALIDURLS;
CREATE TABLE VALIDURLS(

	url VARCHAR(255) PRIMARY KEY ,
	fixed VARCHAR(255)
);


--:name :lvl :cost :type :subtypes :bonus :enhances :critical  :property :powers