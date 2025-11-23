import { useState } from 'react';
import { motion } from 'framer-motion';
import { useNavigate, Link } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { authAPI } from '../lib/api';

const Login = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        username: '',
        password: '',
    });
    const [error, setError] = useState('');

    const loginMutation = useMutation({
        mutationFn: authAPI.login,
        onSuccess: (response) => {
            localStorage.setItem('token', response.data.token);
            // Redirect based on role or to home
            // For now, redirect to home, but we could decode token to check role
            navigate('/');
            window.location.reload(); // To update navbar state
        },
        onError: (err) => {
            setError(err.response?.data?.message || 'Error al iniciar sesión. Verifica tus credenciales.');
        },
    });

    const handleSubmit = (e) => {
        e.preventDefault();
        setError('');
        if (formData.username && formData.password) {
            loginMutation.mutate(formData);
        }
    };

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
    };

    return (
        <div className="min-h-screen pt-24 pb-16 flex items-center justify-center">
            <div className="container mx-auto px-4">
                <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="max-w-md mx-auto"
                >
                    <div className="card glass-strong p-8">
                        <div className="text-center mb-8">
                            <h1 className="text-3xl font-display font-bold mb-2 gradient-text">
                                Bienvenido de nuevo
                            </h1>
                            <p className="text-dark-300">
                                Ingresa a tu cuenta para gestionar noticias
                            </p>
                        </div>

                        {error && (
                            <motion.div
                                initial={{ opacity: 0, height: 0 }}
                                animate={{ opacity: 1, height: 'auto' }}
                                className="bg-red-500/10 border border-red-500/20 text-red-400 px-4 py-3 rounded-xl mb-6 text-sm"
                            >
                                {error}
                            </motion.div>
                        )}

                        <form onSubmit={handleSubmit} className="space-y-6">
                            <div>
                                <label className="block text-sm font-medium text-dark-300 mb-2">
                                    Usuario
                                </label>
                                <input
                                    type="text"
                                    name="username"
                                    value={formData.username}
                                    onChange={handleChange}
                                    className="input"
                                    placeholder="admin"
                                    required
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-dark-300 mb-2">
                                    Contraseña
                                </label>
                                <input
                                    type="password"
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    className="input"
                                    placeholder="••••••••"
                                    required
                                />
                            </div>

                            <button
                                type="submit"
                                disabled={loginMutation.isPending}
                                className="btn-primary w-full flex items-center justify-center gap-2"
                            >
                                {loginMutation.isPending ? (
                                    <>
                                        <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                        </svg>
                                        Iniciando sesión...
                                    </>
                                ) : (
                                    'Iniciar Sesión'
                                )}
                            </button>
                        </form>

                        <div className="mt-6 text-center text-sm text-dark-400">
                            <p>
                                ¿No tienes cuenta?{' '}
                                <Link to="/register" className="text-primary-400 hover:text-primary-300 font-medium">
                                    Regístrate
                                </Link>
                            </p>
                        </div>
                    </div>
                </motion.div>
            </div>
        </div>
    );
};

export default Login;
