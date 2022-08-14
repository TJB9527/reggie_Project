/* 自定义trim */
function trim (str) {  //删除左右两端的空格,自定义的trim()方法
  return str == undefined ? "" : str.replace(/(^\s*)|(\s*$)/g, "")
}

//获取url地址上面的参数
function requestUrlParam(argname){
  var url = location.href   //获取当前请求url的完整url
  var arrStr = url.substring(url.indexOf("?")+1).split("&")  //把“？”前面的请求路径去掉了，仅对请求url后面的参数按“&”分割，得到一个数组
  for(var i =0;i<arrStr.length;i++)
  {
      var loc = arrStr[i].indexOf(argname+"=")  //int indexOf(String str): 返回指定字符在字符串中第一次出现处的索引,如果此字符串中没有这样的字符,则返回 -1
      if(loc!=-1){
          return arrStr[i].replace(argname+"=","").replace("?","")
      }   //请求url的参数中存在“argname=”对应的项则获取“argname=”后的参数值即argname对应值，如果argname是id就获取id值
  }                    //实际这个方法主要就是走add.html页面时获取id，而走add.htm页面只有两个情况，要么添加员工，要么编辑(修改)员工了，对应员工管理页两个button
  return ""
}
