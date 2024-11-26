import axios from 'axios';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Login() {
    
    // 로그인 form
    const [formData, setFormData] = useState({ email: '', password: ''});
    const navigate = useNavigate();

    // bootstrap 유효성 검사 및 제출
    const handleSubmit = async (event) => {
        event.preventDefault();
        
            try {
                // 전송 
                const res = await axios.post('/auth/authenticate', formData, {withCredentials: true});

                if (res.status === 200) {
                    // 응답에서 Access Token 가져와 로컬 변수에 저장
                    const {access_token} = res.data;

                    // Access Token을 axios의 header의 Authorization Bearer Schema에 적용
                    axios.defaults.headers.common['Authorization'] = `Bearer ${access_token}`;

                    navigate("/demo");
                } else {
                    alert("로그인 실패")
                }
            } catch (error) {
            }
    };

    // form 데이터 등록
    function handleChange(event) {
        const { name, value } = event.currentTarget;
        setFormData((prev) => ({
            ...prev,
            [name] : value,
        }));
    }

    return(
        <div>
            <h2>로그인</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor='email'>Email : </label>
                    <input
                        type="email"
                        name="email"
                        id="email"
                        onChange={handleChange}
                    />
                </div>
                <div>
                    <label htmlFor='password'>Password : </label>
                    <input
                        type="password"
                        name="password"
                        id="password"
                        onChange={handleChange}
                        />
                </div>
                <button type='submit' onClick={handleSubmit}>로그인</button>
                <button onClick={()=>{navigate("/register")}}>회원가입</button>
            </form>
        </div>
    )
}

export default Login;