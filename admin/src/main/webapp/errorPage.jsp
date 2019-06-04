<%@ page pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>没有权限访问</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="author" content="Feythin.lau">

    <!-- The styles -->
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/bootstrap.min.css">
    <style type="text/css">
        body {
            padding-bottom: 40px;
        }

        .sidebar-nav {
            padding: 9px 0;
        }

        .showcenter {
            margin-top: 200px;
        }

    </style>
    <!-- The fav icon -->
</head>

<body bgcolor="#FFFFFF">
<div align="center" style="margin-top:8%">
    <div class="jumbotron" style="background:#990033;color: #ffffff ">
        <%String code = request.getParameter("code");%>
        <h1><% if ("403".equals(code)){%>
            对不起，您没有权限访问该页面！
        <%} else if ("404".equals(code)){%>
            该页面不存在！
        <%}%>
        </h1>
    </div>
    <dt>您可以：</dt>
    <dd><a href="javascript:history.go(-1);" style="color: #0000ff" class="back">返回至刚才的页面</a>
</div>

</body>

</html>