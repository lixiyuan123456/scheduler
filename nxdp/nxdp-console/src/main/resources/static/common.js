function checkUserStatusBeforeConfirmResign() {
  $.get('/user/api/getRealTimeUser.do', function(re) {
    if (re.status != 'ok') {
      bootbox.alert(re.message);
      return false;
    }
    if (re.data.status == 1) {
      bootbox.confirm('确认离职？', function(result) {
        if (result) {
          $.post('/resignation/resign.do', function(data) {
            if (data.status != 'ok') {
              bootbox.alert(data.message);
              return false;
            }
            window.location.href = '/resignation';
          });
        }
      });
    }
    if (re.data.status == 0) {
      window.location.href = '/resignation';
    }
  });
}