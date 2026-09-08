import './App.css'
import Show from './pages/show'
import Vote from './pages/vote'
import Waiting from './pages/waiting'
import Set from './pages/set'
import End from './pages/end'
import UserShow from './pages/userShow'
import Login from './pages/login'
import Home from './pages/home'
import { Routes, Route } from "react-router-dom"

function App() {
  return (
    <div>
      <Routes>
        <Route path='/' element={<Home />} />
        <Route path='/login' element={<Login />} />
        <Route path='/vote' element={<Vote />} />
        <Route path='/show' element={<Show />} />
        <Route path='/waiting' element={<Waiting />} />
        <Route path='/set' element={<Set />} />
        <Route path='/end' element={<End />} />
        <Route path='/usershow' element={<UserShow />} />
      </Routes>
    </div>
  )
}

export default App