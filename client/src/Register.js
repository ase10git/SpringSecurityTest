import axios from 'axios';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Register() {
    
    // 회원가입 form
    const [formData, setFormData] = useState({ 
        firstname: '',
        lastname: '',
        email: '', 
        password: ''
    });
    const navigate = useNavigate();

    // bootstrap 유효성 검사 및 제출
    const handleSubmit = async (event) => {
        event.preventDefault();
        
            try {
                // 전송 
                const res = await axios.post('/auth/register', formData);

                if (res.status === 200) {
                    // 응답에서 Access Token 가져와 로컬 변수에 저장
                    const {access_token} = res.data;
                    // Access Token을 axios의 header의 Authorization Bearer Schema에 적용
                    axios.defaults.headers.common['Authorization'] = `Bearer ${access_token}`;

                    alert("가입 성공!");

                    if (!window.confirm("로그인으로 이동할까요?")) {
                        return;
                    }
                    navigate("/login");
                } else {
                    alert('회원가입 실패!');
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
            <h2>회원가입</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor='firstname'>Firstname : </label>
                    <input
                        type="text"
                        name="firstname"
                        id="firstname"
                        onChange={handleChange}
                    />  
                </div>
                <div>
                    <label htmlFor='lastname'>Lastname : </label>
                    <input
                        type="text"
                        name="lastname"
                        id="lastname"
                        onChange={handleChange}
                    />
                </div>
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
                <button type='submit' onClick={handleSubmit}>가입</button>
                <button onClick={()=>{navigate("/login")}}>로그인</button>
            </form>
        </div>
    )
}

export default Register;