
function formatJsonToDateString( json ){
  var date = new Date( json );
  
  var month = date.getMonth() + 1;
  if( month < 10 ){
    month = '0' + month;
  }
  
  var day = date.getDate();
  if( day < 10 ){
    day = '0' + day;
  }
  
  return month + '/' + day + '/' + date.getFullYear();
}

function jsonEscape( str ){
  return str
  .replace(/[\\]/g, '\\\\')
  .replace(/[\"]/g, '\\\"')
  .replace(/[\/]/g, '\\/')
  .replace(/[\b]/g, '\\b')
  .replace(/[\f]/g, '\\f')
  .replace(/[\n]/g, '\\n')
  .replace(/[\r]/g, '\\r')
  .replace(/[\t]/g, '\\t')
  .replace(/[\]]/g, '\\\\]')
  .replace(/[\[]/g, '\\\\[');
};