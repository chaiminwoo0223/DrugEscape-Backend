document.getElementById('login').addEventListener('click', function() {
    // Google OAuth2 로그인 페이지로 리디렉션
    window.location.href = "https://accounts.google.com/o/oauth2/v2/auth?client_id=902025458863-clfbloilmkds2mfs5bj6lhjeg0rhh32c.apps.googleusercontent.com&redirect_uri=http://localhost:8080/drugescape/LoginSignup&response_type=code&scope=https://www.googleapis.com/auth/userinfo.profile%20https://www.googleapis.com/auth/userinfo.email";
});

document.getElementById('logout').addEventListener('click', function() {
    // 로그아웃 API 호출
    fetch('/drugescape/logout', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            accessToken: 'YOUR_ACCESS_TOKEN',
            refreshToken: 'YOUR_REFRESH_TOKEN'
        })
    })
        .then(response => response.json())
        .then(data => console.log(data))
        .catch(error => console.error('Error:', error));
});

document.getElementById('refreshToken').addEventListener('click', function() {
    // 토큰 갱신 API 호출
    fetch('/drugescape/refresh', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            refreshToken: 'YOUR_REFRESH_TOKEN'
        })
    })
        .then(response => response.json())
        .then(data => console.log(data))
        .catch(error => console.error('Error:', error));
});

document.getElementById('goToMain').addEventListener('click', function() {
    // 메인 페이지로 이동
    window.location.href = 'http://localhost:8080/drugescape/main';
});