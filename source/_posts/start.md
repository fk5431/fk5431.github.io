---
title: hexo初步使用
date: 2017-04-17 14:40:10
comments: false
tags: 
- 其他
categories: 
- 其他
keywords: hexo, github
---
## Hexo
Hexo是一个可以快速搭建博客的框架，使用Markdown解析文章，生成静态的网页。（可以部署在github上哦）

### hexo的安装
hexo只依赖于

- Node.js
- Git

上面的两个的安装就不说了，自行百度，hexo基于上述环境可以使用nmp安装

```
npm install -g hexo-cli
```
### hexo的基本命令

- hexo init [folder]   #新建一个网站
- hexo n [layout] <title>    #新建一篇文章
- hexo g   #生成静态文件
- hexo d   #部署网站。
- hexo publish [layout] <filename>   #发表草稿
- hexo s   #启动服务器
- hexo clean   #清除缓存文件 (db.json) 和已生成的静态文件 (public)

### hexo的配置

配置内容来自官网

####网站
|参数|描述|
|---|----|
|title|	网站标题|
|subtitle|	网站副标题|
|description|	网站描述|
|author|	您的名字|
|language|	网站使用的语言|
|timezone	|网站时区。Hexo 默认使用您电脑的时区。时区列表。比如说：America/New_York, Japan, 和 UTC 。|

其中，description主要用于SEO，告诉搜索引擎一个关于您站点的简单描述，通常建议在其中包含您网站的关键词。author参数用于主题显示文章的作者。

####网址
|参数|	描述|	默认值|
|---------|---------|--------|
|url|	网址	|        |
|root	|网站根目录	|          |
|permalink	|文章的 永久链接 格式	|:year/:month/:day/:title/|
|permalink_defaults|	永久链接中各部分的默认值	|     |

>网站存放在子目录
如果您的网站存放在子目录中，例如 http://yoursite.com/blog，则请将您的 url 设为 http://yoursite.com/blog 并把 root 设为 /blog/。

####目录
|参数|	描述|	默认值|
|------|------|-------|
|source_dir|	资源文件夹，这个文件夹用来存放内容。|	source|
|public_dir	|公共文件夹，这个文件夹用于存放生成的站点文件。|	public|
|tag_dir	|标签文件夹|	tags|
|archive_dir|	归档文件夹|	archives|
|category_dir|	分类文件夹	|categories|
|code_dir	|Include code 文件夹|	downloads/code|
|i18n_dir|	国际化（i18n）文件夹	|:lang|
|skip_render	|跳过指定文件的渲染，您可使用 glob 表达式来匹配路径。	|     |

>提示
如果您刚刚开始接触Hexo，通常没有必要修改这一部分的值。

####文章
|参数|	描述	|默认值|
|-----|--------|-----------|
|new_post_name	|新文章的文件名称	|:title.md|
|_layout	|预设布局|	post|
|auto_spacing	|在中文和英文之间加入空格	|false|
|titlecase	|把标题转换为 title case|	false|
|external_link	|在新标签中打开链接|	true|
|filename_case|	把文件名称转换为 (1) 小写或 (2) 大写	|0|
|render_drafts	|显示草稿|	false|
|post_asset_folder	|启动 Asset 文件夹|	false|
|relative_link	|把链接改为与根目录的相对位址|	false|
|future	|显示未来的文章|	true|
|highlight	|代码块的设置|	

>相对地址
默认情况下，Hexo生成的超链接都是绝对地址。例如，如果您的网站域名为example.com,您有一篇文章名为hello，那么绝对链接可能像这样：http://example.com/hello.html，它是绝对于域名的。相对链接像这样：/hello.html，也就是说，无论用什么域名访问该站点，都没有关系，这在进行反向代理时可能用到。通常情况下，建议使用绝对地址。

####分类 & 标签
|参数|	描述|	默认值|
|-------|---------|------------|
|default_category|	默认分类|	uncategorized|
|category_map|	分类别名	|             |
|tag_map|	标签别名	|            |

####日期 / 时间格式         
Hexo 使用 Moment.js 来解析和显示时间。

|参数	|描述	|默认值|
|---------|----------|-----------|
|date_format	|日期格式|	YYYY-MM-DD|
|time_format	|时间格式|	H:mm:ss|

####分页
|参数	|描述	|默认值|
|-------|---------|---------|
|per_page|	每页显示的文章量 (0 = 关闭分页功能)	|10|
|pagination_dir|	分页目录	|page|

####扩展
|参数	|描述|
|----------|----------|
|theme	|当前主题名称。值为false时禁用主题|
|deploy|	部署部分的设置        |


###我的坑
我吧整个hexo init初始化的项目放到github个人blog下……然后github就说解析不了博客了……
然后想了想，我把master分支放hexo生成的public里面的东西，然后新建个分支放hexo的东西，这样就可以在哪里有环境就可以写，然后！！在编译器clone自己的项目，不能在同一个项目文件夹写完然后复制public的文件放到master分支，这样hexo的命令就用不了了，提示加载不到一个模块。
最后吧一个项目clone两次，一个专门该mater分支，一个写…………坑了一上午