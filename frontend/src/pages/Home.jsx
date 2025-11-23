import { motion } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import { newsAPI } from '../lib/api';
import NewsCard from '../components/NewsCard';
import { useState } from 'react';

const Home = () => {
    const [page, setPage] = useState(0);
    const [status] = useState('PUBLISHED');

    const { data, isLoading, error } = useQuery({
        queryKey: ['news', page, status],
        queryFn: () => newsAPI.getAll({ page, size: 12, status }),
    });

    return (
        <div className="min-h-screen pt-24 pb-16">
            <div className="container mx-auto px-4">
                {/* Hero Section */}
                <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="text-center mb-16"
                >
                    <h1 className="text-6xl md:text-7xl font-display font-bold mb-6">
                        <span className="gradient-text">Portal de Noticias</span>
                        <br />
                        <span className="text-dark-200">Automatizado con IA</span>
                    </h1>
                    <p className="text-xl text-dark-300 max-w-2xl mx-auto">
                        Descubre las últimas noticias generadas y curadas por inteligencia artificial,
                        con contenido verificado y actualizado constantemente.
                    </p>

                    {/* Floating particles effect */}
                    <div className="absolute inset-0 overflow-hidden pointer-events-none">
                        {[...Array(20)].map((_, i) => (
                            <motion.div
                                key={i}
                                className="absolute w-2 h-2 bg-primary-500/30 rounded-full"
                                style={{
                                    left: `${Math.random() * 100}%`,
                                    top: `${Math.random() * 100}%`,
                                }}
                                animate={{
                                    y: [0, -30, 0],
                                    opacity: [0.3, 0.6, 0.3],
                                }}
                                transition={{
                                    duration: 3 + Math.random() * 2,
                                    repeat: Infinity,
                                    delay: Math.random() * 2,
                                }}
                            />
                        ))}
                    </div>
                </motion.div>

                {/* Stats */}
                <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.2 }}
                    className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-16"
                >
                    {[
                        { label: 'Noticias Publicadas', value: data?.totalElements || 0, icon: '📰' },
                        { label: 'Generadas con IA', value: Math.floor((data?.totalElements || 0) * 0.7), icon: '🤖' },
                        { label: 'Actualizaciones Diarias', value: '24/7', icon: '⚡' },
                    ].map((stat, index) => (
                        <motion.div
                            key={index}
                            initial={{ opacity: 0, scale: 0.9 }}
                            animate={{ opacity: 1, scale: 1 }}
                            transition={{ delay: 0.3 + index * 0.1 }}
                            className="card-hover text-center"
                        >
                            <div className="text-4xl mb-3">{stat.icon}</div>
                            <div className="text-3xl font-bold gradient-text mb-2">{stat.value}</div>
                            <div className="text-dark-400">{stat.label}</div>
                        </motion.div>
                    ))}
                </motion.div>

                {/* News Grid */}
                <div className="mb-12">
                    <motion.h2
                        initial={{ opacity: 0, x: -20 }}
                        animate={{ opacity: 1, x: 0 }}
                        className="text-3xl font-display font-bold mb-8 flex items-center gap-3"
                    >
                        <div className="w-1 h-8 bg-gradient-to-b from-primary-500 to-accent-500 rounded-full" />
                        Últimas Noticias
                    </motion.h2>

                    {isLoading ? (
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                            {[...Array(6)].map((_, i) => (
                                <div key={i} className="card shimmer h-96" />
                            ))}
                        </div>
                    ) : error ? (
                        <div className="card text-center py-12">
                            <p className="text-red-400 text-lg">Error al cargar las noticias</p>
                        </div>
                    ) : (
                        <>
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                                {data?.content?.map((news, index) => (
                                    <NewsCard key={news.id} news={news} index={index} />
                                ))}
                            </div>

                            {/* Pagination */}
                            {data?.totalPages > 1 && (
                                <div className="flex justify-center gap-2 mt-12">
                                    <button
                                        onClick={() => setPage(Math.max(0, page - 1))}
                                        disabled={page === 0}
                                        className="btn-ghost disabled:opacity-50"
                                    >
                                        Anterior
                                    </button>

                                    <div className="flex items-center gap-2">
                                        {[...Array(data.totalPages)].map((_, i) => (
                                            <button
                                                key={i}
                                                onClick={() => setPage(i)}
                                                className={`w-10 h-10 rounded-lg transition-all ${i === page
                                                        ? 'bg-primary-600 text-white'
                                                        : 'glass hover:bg-white/10'
                                                    }`}
                                            >
                                                {i + 1}
                                            </button>
                                        ))}
                                    </div>

                                    <button
                                        onClick={() => setPage(Math.min(data.totalPages - 1, page + 1))}
                                        disabled={page === data.totalPages - 1}
                                        className="btn-ghost disabled:opacity-50"
                                    >
                                        Siguiente
                                    </button>
                                </div>
                            )}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Home;
