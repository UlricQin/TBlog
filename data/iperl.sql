set names 'utf8';

use iperl;

drop table if exists iperl_users;
create table iperl_users
(
id int unsigned not null auto_increment,
ident varchar(20) not null comment '用户自己给自己起的ID,English,url后缀,也可以用来登陆',
nickname varchar(20) comment 'e.g. iPerler',
pwd varchar(40) not null,
portrait varchar(255) comment '头像',
resume varchar(255) comment '一句话简介',
role tinyint not null comment '用户角色，0是普通用户，-1是不法用户，1是编辑，2是root',
blogcnt int unsigned not null default 0 comment '用户正式发表的博文数目',
ctime timestamp not null default CURRENT_TIMESTAMP,
primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

create unique index idx_user_name on iperl_users ( ident );

/*init pwd is abc*/
insert into iperl_users(ident,nickname,pwd,role,resume) values('iperler','iPerler','9ef2bdeea2b1bae79b9ddb930427d0b2c880bdac',2,'^_^I am admin of iperl.org^_^');

drop table if exists iperl_blog_catalogs;
/*博客分类*/
create table iperl_blog_catalogs
(
id int unsigned not null auto_increment,
name varchar(32) not null comment '分类名称',
ident varchar(50) not null comment '分类英文名称',
dorder smallint not null comment 'display order排序权值',
primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
insert into iperl_blog_catalogs(name,ident,dorder) values('未分类','uncategorized',999);


drop table if exists iperl_blogs;
create table iperl_blogs
(
id int unsigned not null auto_increment,
title varchar(128) not null comment '文章标题',
ident varchar(128) comment 'E文标题',
keywords varchar(255) comment 'for seo,实际可以使用用户填写的tag',
desn varchar(512) comment '导读内容,同时用于html的description',
type tinyint not null default 0 comment '0:原创,1:翻译,2:转载',
url varchar(512) comment '转载的原始地址',
content text comment '文章内容',
user int unsigned not null comment '发布文章的人',
catalog int unsigned not null comment '文章分类',
status tinyint not null comment '文章状态0:草稿,1:发布',
ctime timestamp not null default CURRENT_TIMESTAMP comment '创建时间',
hits mediumint not null default 0 comment '点击次数',
primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

create index idx_blog_user on iperl_blogs ( user );
create index idx_blog_catalog on iperl_blogs ( catalog );


drop table if exists iperl_tags;
create table iperl_tags
(
id int unsigned not null auto_increment,
name varchar(32) not null,
cnt int unsigned not null default 0 comment '这个tag关联的blog数目',
primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

create index idx_tag_name on iperl_tags ( name );

drop table if exists iperl_blog_tags;
create table iperl_blog_tags
(
id int unsigned not null auto_increment,
blog int unsigned not null,
tag int unsigned not null,
primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

create index idx_btag_blog on iperl_blog_tags ( blog );
create index idx_btag_tag on iperl_blog_tags ( tag );

drop table if exists iperl_pages;
create table iperl_pages
(
id int unsigned not null auto_increment,
user int unsigned not null,
content text comment '页面内容',
primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

create index idx_page_user on iperl_pages ( user );

drop table if exists iperl_invitations;
create table iperl_invitations
(
id int unsigned not null auto_increment,
invitation varchar(64) not null comment '邀请码',
used tinyint not null,
primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

create index idx_intivation_inti on iperl_invitations ( invitation );


