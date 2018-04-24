layui.use(['form','laydate','table','laypage','jquery','layer','util'], function(){
    var laydate=layui.laydate
        ,table = layui.table
        ,form=layui.form
        ,layer = parent.layer === undefined ? layui.layer : parent.layer
        ,laypage = layui.laypage
        ,util = layui.util
    $ = layui.jquery;

    table.render({
        skin: 'line' //行边框风格
        ,elem: '#picture'
        ,id: 'pictureId'
        ,url: '/resource/picture/search'
        ,page: true
        ,request: {
            pageName: 'pageNum' //页码的参数名称，默认：page
            ,limitName: 'pageSize' //每页数据量的参数名，默认：limit
        }
        ,response:{
            statusName: 'status' //数据状态的字段名称，默认：code
            ,statusCode: 1 //成功的状态码，默认：0
            ,msgName: 'msg' //状态信息的字段名称，默认：msg
            ,countName: 'total' //数据总数的字段名称，默认：count
            ,dataName: 'data' //数据列表的字段名称，默认：data
        }
        ,cols: [[
            {type:'checkbox'}
            ,{field: 'name', width:'30.5%',title: '图片名称', }
            ,{field: 'codeValue', width:'15%',title: '图片类型' }
            ,{field: 'createDate', width:'15%',title: '创建时间',templet: '<div>{{layui.util.toDateString(d.createDate,"yyyy-MM-dd") }}</div>'}
            ,{field: 'enableFlag', width:'14%',title:'是否启用', templet: '#checkboxTpl' ,align:'center' }
            ,{fixed: 'right' ,width:'20%', toolbar: '#barDemo'}
        ]]
    });


    form.render();

    //日期范围
    laydate.render({
        elem: '#createDate'
        ,range: '~'
    });

    //添加图片
    $(window).one("resize",function(){
        $(".newsAdd_btn").click(function(){
            var index = layui.layer.open({
                title : "添加图片",
                type : 2,
                content : "pictureAdd.html",
                success : function(layero, index){
                    setTimeout(function(){
                        layui.layer.tips('点击此处返回图片列表', '.layui-layer-setwin .layui-layer-close', {
                            tips: 3
                        });
                    },500)
                }
            })
            layui.layer.full(index);
        })
    }).resize();


    //查詢
    $('.search_btn').on('click', function(){

        var date=$("#createDate").val();
        var name=$(".search_input").val();
        var pictureType=$("#pictureType option:selected").val();
        var startDate;
        var endDate;
        if(date.trim()!=""){
            startDate=date.split('~')[0].trim('+');
            endDate=date.split('~')[1].trim('+');
        }

        //执行重载
        table.reload('pictureId', {
            where: {
                name:name,
                type:pictureType,
                startDate:startDate,
                endDate:endDate
            }
        });
    });

    //启用禁用
    form.on('checkbox(lockDemo)', function(obj){
        var enableFlag;
        if(obj.elem.checked==true){
            enableFlag='Y';
        }else {
            enableFlag='N';
        }
        $.ajax({
            type:"PUT",
            url:"/resource/picture/update/enableFlag",
            data: JSON.stringify({"id":this.value,"enableFlag":enableFlag}),
            contentType:"application/json",
            dataType:"json",
            success:function (result) {
                if(result.status==1){
                    if (obj.elem.checked==true){
                        layer.msg("启用成功");
                    }else {
                        layer.msg("禁用成功");
                    }
                }else {
                    layer.msg(result.msg);
                }
            }});

    });

    //监听工具条
    table.on('tool(pic)', function(obj){
        var data = obj.data;
        if(obj.event === 'detail'){
            layer.msg('ID：'+ data.pictureId + ' 的查看操作');
        } else if(obj.event === 'del'){
            layer.confirm('真的删除行么', function(index){
                $.ajax({
                    type:"DELETE",
                    url:"/resource/picture",
                    data: JSON.stringify({"id":data.pictureId}),
                    contentType:"application/json",
                    dataType:"json",
                    success:function (result) {
                        if(result.status==1){
                            layer.msg("删除成功");
                            obj.del();
                            layer.close(index);
                        }
                    }});
            });
        } else if(obj.event === 'edit'){
            $("#pictureId").attr("value",data.pictureId);
            var index = layui.layer.open({
                anim: 1,
                title : "修改图片",
                id:data.pictureId,
                type : 2,
                content : "pictureUpdate.html?",
                success : function(layero, index){
                    setTimeout(function(){
                        layui.layer.tips('点击此处返回图片列表', '.layui-layer-setwin .layui-layer-close', {
                            tips: 3
                        });
                    },500)
                }
            })
            layui.layer.full(index);
        }
    });

});