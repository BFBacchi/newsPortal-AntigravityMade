import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import { useState } from 'react';

const Navbar = () => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const token = localStorage.getItem('token');

    const handleLogout = () => {
        localStorage.removeItem('token');
        window.location.href = '/';
    };

    return (
        <motion.nav
            initial={{ y: -100 }}
            animate={{ y: 0 }}
            className="fixed top-0 left-0 right-0 z-50 glass-strong border-b border-white/10"
        >
            <div className="container mx-auto px-4">
                <div className="flex items-center justify-between h-20">
                    {/* Logo */}
                    <Link to="/" className="flex items-center gap-3 group">
                        <div className="relative">
                            <div className="absolute inset-0 bg-gradient-to-r from-primary-500 to-accent-500 rounded-xl blur-lg opacity-50 group-hover:opacity-75 transition-opacity" />
                            <div className="relative bg-gradient-to-r from-primary-600 to-accent-600 p-3 rounded-xl">
                                <svg className="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 20 20">
                                    <path fillRule="evenodd" d="M2 5a2 2 0 012-2h8a2 2 0 012 2v10a2 2 0 002 2H4a2 2 0 01-2-2V5zm3 1h6v4H5V6zm6 6H5v2h6v-2z" clipRule="evenodd" />
                                    <path d="M15 7h1a2 2 0 012 2v5.5a1.5 1.5 0 01-3 0V7z" />
                                </svg>
                            </div>
                        </div>
                        <span className="text-2xl font-display font-bold gradient-text">
                            NewsPortal
                        </span>
                    </Link>

                    {/* Desktop Navigation */}
                    <div className="hidden md:flex items-center gap-8">
                        <Link to="/" className="text-dark-200 hover:text-white transition-colors font-medium">
                            Inicio
                        </Link>
                        <Link to="/news" className="text-dark-200 hover:text-white transition-colors font-medium">
                            Noticias
                        </Link>

                        {token ? (
                            <>
                                <Link to="/backoffice" className="text-dark-200 hover:text-white transition-colors font-medium">
                                    Backoffice
                                </Link>
                                <button
                                    onClick={handleLogout}
                                    className="btn-ghost"
                                >
                                    Cerrar Sesión
                                </button>
                            </>
                        ) : (
                            <Link to="/login" className="btn-primary">
                                Iniciar Sesión
                            </Link>
                        )}
                    </div>

                    {/* Mobile menu button */}
                    <button
                        onClick={() => setIsMenuOpen(!isMenuOpen)}
                        className="md:hidden p-2 rounded-lg glass hover:bg-white/10 transition-colors"
                    >
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            {isMenuOpen ? (
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            ) : (
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                            )}
                        </svg>
                    </button>
                </div>

                {/* Mobile menu */}
                {isMenuOpen && (
                    <motion.div
                        initial={{ opacity: 0, height: 0 }}
                        animate={{ opacity: 1, height: 'auto' }}
                        exit={{ opacity: 0, height: 0 }}
                        className="md:hidden pb-4 border-t border-white/10 mt-2"
                    >
                        <div className="flex flex-col gap-4 pt-4">
                            <Link to="/" className="text-dark-200 hover:text-white transition-colors font-medium">
                                Inicio
                            </Link>
                            <Link to="/news" className="text-dark-200 hover:text-white transition-colors font-medium">
                                Noticias
                            </Link>

                            {token ? (
                                <>
                                    <Link to="/backoffice" className="text-dark-200 hover:text-white transition-colors font-medium">
                                        Backoffice
                                    </Link>
                                    <button
                                        onClick={handleLogout}
                                        className="btn-ghost text-left"
                                    >
                                        Cerrar Sesión
                                    </button>
                                </>
                            ) : (
                                <Link to="/login" className="btn-primary">
                                    Iniciar Sesión
                                </Link>
                            )}
                        </div>
                    </motion.div>
                )}
            </div>
        </motion.nav>
    );
};

export default Navbar;
