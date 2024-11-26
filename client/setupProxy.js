// src/setupProxy.js
const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
    app.use(
        '/api', // proxy가 필요한 요청 경로, /api/test와 같이 /api로 시작하는 요청들 포함
        createProxyMiddleware({
            target: `http://localhost:9000/`, // 타겟이 되는 api url
            changeOrigin: true, // 대상 서버 구성에 따라 host header 변경 설정
        })
    );
};