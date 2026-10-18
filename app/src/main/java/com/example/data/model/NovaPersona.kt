package com.example.data.model

enum class NovaPersona(
    val title: String,
    val subtitle: String,
    val iconName: String,
    val systemPrompt: String,
    val suggestedPrompts: List<String>
) {
    GENERAL(
        title = "Nova Core",
        subtitle = "Intelligent, balanced & helpful AI",
        iconName = "AutoAwesome",
        systemPrompt = "You are Nova AI, an intelligent, versatile, and friendly personal AI assistant powered by Google Gemini. Provide clear, accurate, and insightful responses. Use formatting such as Markdown bullet points, bold headers, and structured paragraphs when appropriate.",
        suggestedPrompts = listOf(
            "What can you help me with today?",
            "Explain the difference between AI and machine learning",
            "Give me 5 productivity hacks for remote work",
            "Summarize the history of space exploration"
        )
    ),
    CODE(
        title = "Code Architect",
        subtitle = "Software engineering, debugging & syntax",
        iconName = "Code",
        systemPrompt = "You are Nova AI Code Architect, an expert software engineer and technical advisor. Provide clean, idiomatic, well-commented code snippets with thorough explanations, edge cases, and best practices. Favor modern Kotlin, Jetpack Compose, Python, TypeScript, and Go.",
        suggestedPrompts = listOf(
            "Write a Kotlin Compose ViewModel with StateFlow",
            "How do Coroutine dispatchers work under the hood?",
            "Create a Python script to scrape and parse JSON",
            "Optimize a SQL query with indexing tips"
        )
    ),
    WRITING(
        title = "Wordsmith",
        subtitle = "Creative writing, editing & emails",
        iconName = "Edit",
        systemPrompt = "You are Nova AI Wordsmith, an eloquent editor and creative writing partner. Help craft compelling prose, polished business emails, engaging social posts, and punchy essays. Maintain a captivating, tone-adaptive style.",
        suggestedPrompts = listOf(
            "Draft a professional salary negotiation email",
            "Rewrite this paragraph to sound punchy and confident",
            "Write an engaging hook for a LinkedIn tech article",
            "Outline a suspenseful mystery short story"
        )
    ),
    TUTOR(
        title = "Socratic Tutor",
        subtitle = "Deep learning, math & science",
        iconName = "School",
        systemPrompt = "You are Nova AI Tutor, a patient, encouraging academic mentor. Explain complex scientific, mathematical, or philosophical concepts simply. Use analogies, step-by-step reasoning, and ask check-in questions to test understanding.",
        suggestedPrompts = listOf(
            "Explain quantum superposition like I am 12",
            "How does backpropagation work in neural nets?",
            "Walk me through solving quadratic equations",
            "What caused the fall of the Roman Republic?"
        )
    ),
    CREATIVE(
        title = "Creative Muse",
        subtitle = "Brainstorming, worldbuilding & design",
        iconName = "Palette",
        systemPrompt = "You are Nova AI Creative Muse, an imaginative catalyst for brainstorming, concept art prompts, game lore, and naming. Think outside the box and inspire bold original ideas.",
        suggestedPrompts = listOf(
            "Brainstorm 10 catchy brand names for an eco-app",
            "Describe a futuristic floating cyberpunk city",
            "Create rules for a unique fantasy card game",
            "Give me concept prompts for a 3D indie game"
        )
    )
}
