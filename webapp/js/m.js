function alertMsg(msg){
	art.dialog({
    title:"提示信息",
    okValue:"确定",
    content:"" + msg,
	ok:function(){return true;}
    });
}
function alertMsgCallback(msg,callback){
	art.dialog({
    title:"提示信息",
    okValue:"确定",
    content:"" + msg,
	ok:callback
    });
}
function delObj(url){
	if(confirm('Really to delete?')){
		jQuery.post(url,function(data){
    		if(data.msg.length>0){
    			alertMsg(data.msg);
    		}else{
    			alertMsgCallback("Delete Success",function(){
					location.reload();
				});
    		}
		},'json');
	}
}




