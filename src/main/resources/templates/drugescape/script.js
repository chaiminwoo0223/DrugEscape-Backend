document.getElementById('loginButton').addEventListener('click', function() {
    const clientId = '902025458863-clfbloilmkds2mfs5bj6lhjeg0rhh32c.apps.googleusercontent.com';
    const redirectUri = 'http://localhost:8080/drugescape/callback';
    const responseType = 'code';
    const scope = 'https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email';
    const authUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=${responseType}&scope=${scope}`;

    window.location.href = authUrl; // Redirect to Google's OAuth 2.0 server
});

// Assuming you have a way to get tokens from your server to here after login
// For demonstration, we'll simulate storing and displaying tokens
function storeAndDisplayTokens(accessToken, refreshToken) {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);

    document.getElementById('accessToken').textContent = `Access Token: ${accessToken}`;
    document.getElementById('refreshToken').textContent = `Refresh Token: ${refreshToken}`;
}

// This is a mock function to demonstrate token handling
// In a real application, you would retrieve these from your server after login
storeAndDisplayTokens('your_access_token_here', 'your_refresh_token_here');