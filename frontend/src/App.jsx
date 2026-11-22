import { Navigate, Route, Routes } from 'react-router-dom'
import RequireAuth from './auth/RequireAuth.jsx'
import Layout from './components/Layout.jsx'
import AccountsPage from './pages/AccountsPage.jsx'
import CompetenciesPage from './pages/CompetenciesPage.jsx'
import EditStudentPage from './pages/EditStudentPage.jsx'
import GroupSetPage from './pages/GroupSetPage.jsx'
import ImportPage from './pages/ImportPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'
import ProjectFormPage from './pages/ProjectFormPage.jsx'
import ProjectPage from './pages/ProjectPage.jsx'
import ProjectsPage from './pages/ProjectsPage.jsx'
import StructurePage from './pages/StructurePage.jsx'
import StudentDetailPage from './pages/StudentDetailPage.jsx'
import StudentsPage from './pages/StudentsPage.jsx'

export default function App() {
  return (
    <Routes>
      <Route path="login" element={<LoginPage />} />
      <Route
        element={
          <RequireAuth>
            <Layout />
          </RequireAuth>
        }
      >
        <Route index element={<Navigate to="/students" replace />} />
        <Route path="students" element={<StudentsPage />} />
        <Route path="students/:id" element={<StudentDetailPage />} />
        <Route path="students/:id/edit" element={<EditStudentPage />} />
        <Route path="projects" element={<ProjectsPage />} />
        <Route path="projects/new" element={<ProjectFormPage />} />
        <Route path="projects/:id" element={<ProjectPage />} />
        <Route path="projects/:id/edit" element={<ProjectFormPage />} />
        <Route path="group-sets/:id" element={<GroupSetPage />} />
        <Route path="competencies" element={<CompetenciesPage />} />
        <Route path="import" element={<ImportPage />} />
        <Route
          path="structure"
          element={
            <RequireAuth adminOnly>
              <StructurePage />
            </RequireAuth>
          }
        />
        <Route
          path="accounts"
          element={
            <RequireAuth adminOnly>
              <AccountsPage />
            </RequireAuth>
          }
        />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
