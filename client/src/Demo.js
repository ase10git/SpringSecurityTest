import axios from "axios";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

function Demo() {

    const [hello, setHello] = useState('');
    const navigate = useNavigate();

    // token 재발급
    const refreshToken = async () => {
        try {
            const res = await axios.post('/auth/refresh-token', {}, {withCredentials: true});
            // 응답에서 Access Token 가져와 로컬 변수에 저장
            const {access_token} = res.data;
            
            // Access Token을 axios의 header의 Authorization Bearer Schema에 적용
            axios.defaults.headers.common['Authorization'] = `Bearer ${access_token}`;
        } catch (error) {
            alert("로그인을 다시 해주세요");
            navigate('/login');
        }
    }

    // 결과에 따라 재요청을 처리
    const fetchWithRetry = async (url, options = {}) => {
        try {
            const res = await axios.get(url, options);
            return res.data;
        } catch (error) {
            // 응답이 401이나 403이면 token이 없는 상태
            if (error.response && (error.response.status === 401 || error.response.status === 403)) {
                // token 재발급
                await refreshToken();
                // 다시 자원 요청
                const res = await axios.get(url, options);
                return res.data;
            } else {
                throw error;
            }
        }
    }

    // secured endpoint의 자원 요청하기
    const getResource = async () => {
        // 자원 요청
        try {
            const data = await fetchWithRetry('/demo-controller');
            setHello(data);
        } catch (error) {
            setHello("Not Authorized");
        }
    }

    // 로그아웃
    const logout = async () => {
        try {
            await axios.get("/auth/logout");
        } catch (error) {
        }
    }

	// 최초 렌더링 때만 자원 요청 함수 실행
    useEffect(()=>{
        getResource();
    }, []);

    return(
        <div>
            <h2>Demo Page</h2>
            <button onClick={()=>{navigate("/register")}}>회원가입</button>
            <button onClick={()=>{navigate("/login")}}>로그인</button>
            <button onClick={logout}>로그아웃</button>
            <p>{hello}</p>
        </div>
    )
}

export default Demo;