import './App.css';
import { Route, Routes } from 'react-router-dom';
import Login from './Login';
import Register from './Register';
import Demo from './Demo';

function App() {
  return (
    <div className="App">
      <Routes>
        <Route path='/login' Component={Login}></Route>
        <Route path='/register' Component={Register}></Route>
        <Route path='/demo' Component={Demo}/>
      </Routes>
    </div>
  ); 
}

export default App;
